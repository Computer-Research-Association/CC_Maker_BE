package com.ccapp.ccgo.team.dto;

import com.ccapp.ccgo.common.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SurveyStatusDto {
    private Long userId;
    private String userName;
    private boolean isSurveyCompleted;
}
