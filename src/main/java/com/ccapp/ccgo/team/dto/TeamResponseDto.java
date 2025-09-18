package com.ccapp.ccgo.team.dto;

import com.ccapp.ccgo.common.Role;
import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
@Builder
public class TeamResponseDto {
    private Long teamId;
    private String teamName;
    private Role role;

}
