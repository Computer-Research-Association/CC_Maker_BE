package com.ccapp.ccgo.mission.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MissionHistoryDto {
    
    private Long id;
    private Long subGroupId;
    private String subGroupName; // 서브그룹 이름 (필요시)
    private Long teamId;
    private String teamName; // 팀 이름
    private Long userId;
    private String userName; // 사용자 이름
    private Long missionTemplateId;
    private String missionTitle; // 미션 제목
    private String missionDescription; // 미션 설명
    private Integer missionScore; // 미션 점수
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
}
