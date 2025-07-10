package com.ccapp.ccgo.auth.controller;

import com.ccapp.ccgo.auth.dto.LoginRequestDto;
import com.ccapp.ccgo.auth.dto.LoginResponseDto;
import com.ccapp.ccgo.auth.dto.TokenResponseDto;
import com.ccapp.ccgo.auth.jwt.JwtProvider;
import com.ccapp.ccgo.auth.jwt.LoginUserDetails;
import com.ccapp.ccgo.auth.service.LoginUserDetailsService;
import com.ccapp.ccgo.team.repository.TeamMemberRepository;
import com.ccapp.ccgo.team.entity.TeamMember;
import com.ccapp.ccgo.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import com.ccapp.ccgo.team.repository.TeamRepository;
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

import java.util.List;
import java.util.Map;

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

    //@authenticatedPrincipal UserDetails authenticatedPrincipal
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto requestDto ) {
        log.info("로그인 요청 받음: {}", requestDto.getEmail());
        log.info("로그인 요청 받음: {}", requestDto.getPassword());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            requestDto.getEmail(), requestDto.getPassword()
                    )
            );

            String accessToken = jwtProvider.createAccessToken(authentication);
            String refreshToken = jwtProvider.createRefreshToken(authentication);
            LoginUserDetails userDetails = (LoginUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();
            log.info("🔍 로그인한 사용자: {}", user.getEmail());

            // 유저가 활성화된 팀멤버 목록 조회
            List<TeamMember> teamMembers = teamMemberRepository.findAllByUserAndIsActiveTrue(user);


            // 팀멤버 정보를 LoginResponseDto.TeamInfo 리스트로 변환
            List<LoginResponseDto.TeamInfo> teams = teamMembers.stream()
                    .map(tm -> LoginResponseDto.TeamInfo.builder()
                            .teamId(tm.getTeam().getTeamId())
                            .teamName(tm.getTeam().getTeamName())
                            .role(tm.getRole().name())
                            .isSurveyCompleted(tm.isSurveyCompleted()) // 팀별 설문 완료 여부
                            .build())
                    .toList();

            HttpHeaders headers = createTokenCookies(accessToken, refreshToken);

            LoginResponseDto response = LoginResponseDto.builder()
                    .userId(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .teams(teams)
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(response);

        } catch (BadCredentialsException e) {
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
                .body(new TokenResponseDto(newAccessToken, newRefreshToken));
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


