package com.ccapp.ccgo.mission.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubGroupScoreDto {
    private Long subGroupId;
    private String name;   // "1조" 등
    private int score;     // 현재 학점
    private List<String> members;  // ✅ 서브그룹 소속 유저 이름
}
