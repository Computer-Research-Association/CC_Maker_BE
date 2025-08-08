package com.ccapp.ccgo.mission.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SubGroupMissionDto {

    private Long subGroupMissionId;
    private Long missionTemplateId;
    private String title;
    private String description;
    private Integer score;
    private boolean completed;

}
