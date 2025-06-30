package com.ccapp.ccgo.controller;

import com.ccapp.ccgo.jwt.LoginUserDetailsService;
import com.ccapp.ccgo.repository.TeamMemberRepository;
import com.ccapp.ccgo.team.TeamMember;
import com.ccapp.ccgo.user.User;
import com.ccapp.ccgo.dto.LoginRequestDto;
import com.ccapp.ccgo.dto.LoginResponseDto;
import com.ccapp.ccgo.jwt.JwtProvider;
import com.ccapp.ccgo.jwt.LoginUserDetails;
import lombok.extern.slf4j.Slf4j;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final TeamMemberRepository teamMemberRepository;
    private final LoginUserDetailsService loginUserDetailsService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto requestDto) {
        log.info("로그인 요청 받음: {}", requestDto.getEmail());

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

        } catch (Exception e) {
            log.error("로그인 중 오류 발생", e);
            return ResponseEntity.status(401).body("이메일 또는 비밀번호가 잘못되었습니다.");
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


