package com.ccapp.ccgo.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JwtToken {
    private String grantType;     // 예: "Bearer"
    private String accessToken;
    private String refreshToken;  // 사용하지 않으면 null 또는 빈 문자열
}
