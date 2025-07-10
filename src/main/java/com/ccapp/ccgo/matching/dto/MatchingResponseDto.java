package com.ccapp.ccgo.matching.dto;

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
