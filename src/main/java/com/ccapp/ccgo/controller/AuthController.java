package com.ccapp.ccgo.controller;

import com.ccapp.ccgo.dto.LoginRequestDto;
import com.ccapp.ccgo.jwt.JwtProvider;
import com.ccapp.ccgo.jwt.JwtToken;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    @PostMapping("/login")
    public ResponseEntity<JwtToken> login(@RequestBody LoginRequestDto requestDto) {
        // 사용자 인증
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        requestDto.getEmail(), requestDto.getPassword()
                )
        );

        // JWT 토큰 생성
        String accessToken = jwtProvider.createAccessToken(authentication);
        String refreshToken = jwtProvider.createRefreshToken(authentication); // 선택 사항

        JwtToken token = new JwtToken("Bearer", accessToken, refreshToken);
        return ResponseEntity.ok(token);
    }
}
