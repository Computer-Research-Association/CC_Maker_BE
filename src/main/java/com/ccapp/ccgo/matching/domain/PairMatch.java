package com.ccapp.ccgo.matching.domain;

import com.ccapp.ccgo.team.entity.TeamMember;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter

public class PairMatch {
    private TeamMember male;       // 남자 멤버
    private TeamMember female;     // 여자 멤버
    private double totalScore;     // 매칭 점수(MBTI + 설문 등 종합)
}
