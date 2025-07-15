package com.ccapp.ccgo.mission.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TeamMissionDto {
    private Long missionId;       // 미션 템플릿 ID
    private String title;         // 미션 제목
    private String description;   // 미션 내용
    private int score;            // 미션 점수
}
