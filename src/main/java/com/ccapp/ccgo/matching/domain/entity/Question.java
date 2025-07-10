package com.ccapp.ccgo.matching.domain.entity;

import com.ccapp.ccgo.team.entity.Team;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "question")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 어떤 팀에서 작성한 질문인지
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    /**
     * 질문 텍스트
     * ex) "당신의 주말 취미는 무엇인가요?"
     */
    @Column(nullable = false, length = 1000)
    private String text;
}
