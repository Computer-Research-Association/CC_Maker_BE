package com.ccapp.ccgo.team.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TeamMatchingStatusDto {
    private Long teamId;
    private String teamName;
    private boolean matchingStarted;
}
