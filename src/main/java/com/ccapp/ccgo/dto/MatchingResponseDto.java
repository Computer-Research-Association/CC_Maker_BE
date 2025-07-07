package com.ccapp.ccgo.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchingResponseDto {
    private Long teamId;
    private String teamName;
    private List<MatchingResultDto> subGroups;
}
