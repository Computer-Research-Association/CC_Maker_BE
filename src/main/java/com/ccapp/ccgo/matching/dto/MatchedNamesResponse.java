package com.ccapp.ccgo.matching.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class MatchedNamesResponse {
    private Long teamId;
    private Long subGroupId;
    private List<String> matchedNames;
}
