package com.ccapp.ccgo.controller;

import com.ccapp.ccgo.User;
import com.ccapp.ccgo.dto.LoginRequestDto;
import com.ccapp.ccgo.dto.LoginResponseDto;
import com.ccapp.ccgo.jwt.JwtProvider;
import com.ccapp.ccgo.jwt.JwtToken;
import com.ccapp.ccgo.jwt.LoginUserDetails;
import jakarta.validation.Valid;
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
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto requestDto) {
        System.out.println("로그인 요청 도착: " + requestDto.getEmail() + requestDto.getPassword());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        requestDto.getEmail(), requestDto.getPassword()
                )
        );

        String accessToken = jwtProvider.createAccessToken(authentication);
        String refreshToken = jwtProvider.createRefreshToken(authentication);

        System.out.println("Principal 클래스 =" + authentication.getPrincipal().getClass());

        User user = (User) authentication.getPrincipal(); // UserDetails 상속 객체라면

        LoginResponseDto response = LoginResponseDto.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .build();

        return ResponseEntity.ok(response);
    }

}
