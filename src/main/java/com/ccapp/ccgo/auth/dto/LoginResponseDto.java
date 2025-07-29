package com.ccapp.ccgo.auth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {
    private String grantType;   // Bearer
    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String email;
    private String name;
    private List<TeamInfo> teams;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamInfo {
        private Long teamId;
        private String teamName;
        private String role;  // 팀 내 역할 (예: "LEADER", "MEMBER")
        private boolean isSurveyCompleted;
    }
}
