package com.ccapp.ccgo.mission.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoreboardResponseDto {
    private int minScore;                     // 팀 최소 학점
    private SubGroupScoreDto mySubGroup;      // 현재 유저의 서브그룹 점수
    private List<SubGroupScoreDto> otherSubGroups; // 다른 서브그룹들 점수

}
