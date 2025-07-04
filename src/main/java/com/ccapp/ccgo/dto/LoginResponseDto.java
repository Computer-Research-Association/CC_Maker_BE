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

    //이 정보가 필요할지는 고민해봐야한다.
    private Long teamId;
    private String teamName;

    private boolean isInterestCompleted;  // 기본값 false
}
