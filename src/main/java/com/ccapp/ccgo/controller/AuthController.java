package com.ccapp.ccgo.controller;

import com.ccapp.ccgo.jwt.LoginUserDetailsService;
import com.ccapp.ccgo.repository.TeamMemberRepository;
import com.ccapp.ccgo.team.Team;
import com.ccapp.ccgo.team.TeamMember;
import com.ccapp.ccgo.user.User;
import com.ccapp.ccgo.dto.LoginRequestDto;
import com.ccapp.ccgo.dto.LoginResponseDto;
import com.ccapp.ccgo.jwt.JwtProvider;
import com.ccapp.ccgo.jwt.LoginUserDetails;
import lombok.extern.slf4j.Slf4j;
import com.ccapp.ccgo.repository.TeamRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final TeamMemberRepository teamMemberRepository;
    private final LoginUserDetailsService loginUserDetailsService;
    private final TeamRepository teamRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto requestDto) {
        log.info("로그인 요청 받음: {}", requestDto.getEmail());
        log.info("로그인 요청 받음: {}", requestDto.getPassword());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            requestDto.getEmail(), requestDto.getPassword()
                    )
            );
            log.info("✅ 인증 성공: {}", authentication.getName());

            String accessToken = jwtProvider.createAccessToken(authentication);
            String refreshToken = jwtProvider.createRefreshToken(authentication);
            LoginUserDetails userDetails = (LoginUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();
            log.info("🔍 로그인한 사용자: {}", user.getEmail());

            // ✅ 팀이 없으면 팀 생성 + 팀장 등록
            Optional<TeamMember> existingTeamMember = teamMemberRepository.findByUserAndIsActiveTrue(user);
            if (existingTeamMember.isEmpty() && user.getRole().equals("TeamLeader")) {

                // 1. 새 팀 생성
                Team team = new Team();
                team.setTeamName(user.getName() + "의 팀"); // 원하는 네이밍 규칙 사용
                team.setCreatedAt(LocalDateTime.now());
                team.setCreatedBy(user.getId());
                teamRepository.save(team);

                // 2. 팀장 본인을 팀원으로 등록
                TeamMember teamMember = new TeamMember();
                teamMember.setUser(user);
                teamMember.setTeam(team);
                teamMember.setRole("TeamLeader");  // 또는 enum 등
                teamMember.setActive(true);
                teamMember.setJoinedAt(LocalDateTime.now());
                teamMemberRepository.save(teamMember);

                log.info("🆕 새 팀 생성 및 팀장 등록 완료");
            }

            // ✅ 다시 조회 (혹은 Optional.get으로 바로 사용 가능)
            TeamMember teamMember = teamMemberRepository.findByUserAndIsActiveTrue(user)
                    .orElseThrow(() -> new RuntimeException("소속된 팀이 없습니다."));

            HttpHeaders headers = createTokenCookies(accessToken, refreshToken);

            LoginResponseDto response = LoginResponseDto.builder()
                    .userId(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .teamId(teamMember.getTeam().getTeamId())
                    .teamName(teamMember.getTeam().getTeamName())
                    .role(teamMember.getRole())
                    .build();

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(response);

        } catch (BadCredentialsException e) {
            log.error("❌ 로그인 실패: 자격 증명 오류", e);
            return ResponseEntity.status(401).body(Map.of("message", "이메일 또는 비밀번호가 잘못되었습니다."));
        } catch (RuntimeException e) {
            log.error("❌ 로그인 중 런타임 예외", e);
            return ResponseEntity.status(400).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ 로그인 중 알 수 없는 예외", e);
            return ResponseEntity.status(500).body(Map.of("message", "서버 오류가 발생했습니다."));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@CookieValue(value = "refreshToken", required = false) String refreshToken) {
        if (refreshToken == null || !jwtProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(401).body("리프레시 토큰이 없거나 유효하지 않습니다.");
        }

        String email = jwtProvider.getEmailFromToken(refreshToken);
        LoginUserDetails userDetails = (LoginUserDetails) loginUserDetailsService.loadUserByUsername(email);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );

        String newAccessToken = jwtProvider.createAccessToken(authentication);
        String newRefreshToken = jwtProvider.createRefreshToken(authentication);

        HttpHeaders headers = createTokenCookies(newAccessToken, newRefreshToken);

        return ResponseEntity.ok()
                .headers(headers)
                .body("토큰이 재발급 되었습니다.");
    }

    /**
     * accessToken, refreshToken을 HttpOnly 쿠키로 설정하는 공통 메서드
     */
    private HttpHeaders createTokenCookies(String accessToken, String refreshToken) {
        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .path("/")
                .maxAge(60 * 60) // 1시간
                .secure(false) // 운영 배포 시 true로
                .sameSite("Lax")
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .path("/")
                .maxAge(7 * 24 * 60 * 60) // 7일
                .secure(false)
                .sameSite("Lax")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
        return headers;
    }
}


