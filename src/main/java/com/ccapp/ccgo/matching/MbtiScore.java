package com.ccapp.ccgo.matching;

import jakarta.persistence.*;
import lombok.*;

@Entity
@IdClass(MbtiScoreId.class)
@Table(name = "mbti_score")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MbtiScore {

    @Id
    @Column(name = "from_mbti", length = 4)
    private String fromMbti;

    @Id
    @Column(name = "to_mbti", length = 4)
    private String toMbti;

    @Column(nullable = false)
    private Integer score;
}
