package com.ccapp.ccgo.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponseDto {
    private String grantType;   // Bearer
    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String email;
    private String name;
    private String role;
}
