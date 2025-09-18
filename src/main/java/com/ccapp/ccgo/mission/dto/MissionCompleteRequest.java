package com.ccapp.ccgo.mission.dto;

import lombok.Data;

@Data
public class MissionCompleteRequest {
    private Long teamId;
    private Long subGroupId;  // nullable
    private Long missionId;
}
