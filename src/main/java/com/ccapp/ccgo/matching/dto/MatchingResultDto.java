package com.ccapp.ccgo.matching.dto;

import com.ccapp.ccgo.user.dto.UserResponseDto;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchingResultDto {
    private Long subGroupId;              // SubGroup PK
    private String groupName;             // SubGroup 이름 (팀이름+인덱스)
    private List<UserResponseDto> members; // 그룹 멤버 리스트
}
