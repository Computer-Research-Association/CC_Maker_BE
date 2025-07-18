package com.ccapp.ccgo.mission.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubGroupScoreDto {
    private Long subGroupId;
    private String name;   // "1조" 등
    private int score;     // 현재 학점
}
