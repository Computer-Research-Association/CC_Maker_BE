package com.ccapp.ccgo.auth.controller;

import com.ccapp.ccgo.auth.dto.LoginRequestDto;
import com.ccapp.ccgo.auth.dto.LoginResponseDto;
import com.ccapp.ccgo.auth.dto.TokenResponseDto;
import com.ccapp.ccgo.auth.jwt.JwtProvider;
import com.ccapp.ccgo.auth.service.AuthService;
import com.ccapp.ccgo.auth.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.cookie.secure:true}")
    private boolean cookieSecure;

    @Value("${app.cookie.same-site:Lax}")
    private String cookieSameSite;

    @Value("${jwt.access-token-expiration:3600}") // 초 단위
    private long accessTokenMaxAge;

    @Value("${jwt.refresh-token-expiration:604800}") // 초 단위
    private long refreshTokenMaxAge;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto requestDto) {
        String maskedEmail = maskEmail(requestDto.getEmail());
        log.info("로그인 요청 받음: {}", maskedEmail);

        try {
            LoginResponseDto response = authService.login(requestDto.getEmail(), requestDto.getPassword());

            HttpHeaders headers = createTokenCookies(response.getAccessToken(), response.getRefreshToken());

            log.info("발급된 쿠키: {}", headers.get(HttpHeaders.SET_COOKIE));


            // 응답 바디에 토큰도 포함해서 내려줌 (Expo Go용)
            Map<String, Object> responseBody = Map.of(
                    "message", "로그인 성공",
                    "data", LoginResponseDto.builder()
                            .userId(response.getUserId())
                            .email(response.getEmail())
                            .name(response.getName())
                            .teams(response.getTeams())
                            .build(),
                    "accessToken", response.getAccessToken(),
                    "refreshToken", response.getRefreshToken()
            );

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(responseBody);

        } catch (BadCredentialsException e) {
            log.warn("로그인 실패 - 잘못된 이메일 또는 비밀번호: {}", maskedEmail);
            return ResponseEntity.status(401).body(Map.of("message", "이메일 또는 비밀번호가 잘못되었습니다."));
        } catch (Exception e) {
            log.error("❌ 로그인 중 오류 발생", e);
            return ResponseEntity.status(500).body(Map.of("message", "서버 오류가 발생했습니다."));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(
            @CookieValue(value = "accessToken", required = false) String accessToken,
            @CookieValue(value = "refreshToken", required = false) String refreshToken) {
        try {
            if (accessToken == null || refreshToken == null) {
                return ResponseEntity.status(401).body(Map.of("message", "토큰이 없습니다."));
            }

            TokenResponseDto tokenResponse = authService.refreshToken(accessToken, refreshToken);
            HttpHeaders headers = createTokenCookies(tokenResponse.getAccessToken(), tokenResponse.getRefreshToken());

            log.info("새로운 쿠키: {}", headers.get(HttpHeaders.SET_COOKIE));

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(Map.of(
                        "message", "토큰 갱신 성공",
                        "accessToken", tokenResponse.getAccessToken(),
                        "refreshToken", tokenResponse.getRefreshToken()
                    ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ 토큰 갱신 중 오류 발생", e);
            return ResponseEntity.status(500).body(Map.of("message", "서버 오류가 발생했습니다."));
        }
    }


    private HttpHeaders createTokenCookies(String accessToken, String refreshToken) {
        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .path("/")
                .maxAge(accessTokenMaxAge)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .path("/")
                .maxAge(refreshTokenMaxAge)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
        return headers;
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "unknown";
        String[] parts = email.split("@");
        return parts[0].charAt(0) + "***@" + parts[1];
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue(value = "refreshToken", required = false) String refreshToken) {
        if (refreshToken == null || !jwtProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(401).body(Map.of("message", "유효하지 않은 리프레시 토큰입니다."));
        }

        // Redis에서 Refresh Token 삭제
        String email = jwtProvider.getEmailFromToken(refreshToken);
        refreshTokenService.deleteRefreshToken(email);

        // 쿠키 삭제 (maxAge=0)
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, ResponseCookie.from("accessToken", "")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .build()
                .toString());
        headers.add(HttpHeaders.SET_COOKIE, ResponseCookie.from("refreshToken", "")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .build()
                .toString());

        return ResponseEntity.ok()
                .headers(headers)
                .body(Map.of("message", "로그아웃 처리 완료"));
    }



}
