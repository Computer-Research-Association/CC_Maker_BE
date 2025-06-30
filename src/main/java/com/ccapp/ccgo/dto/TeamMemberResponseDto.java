package com.ccapp.ccgo.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeamMemberResponseDto {
    private Long userId;
    private String userName;
    private String role;
}
