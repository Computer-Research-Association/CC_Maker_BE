package com.ccapp.ccgo.matching;

import com.ccapp.ccgo.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "answer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 답변한 사용자
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 어떤 질문에 대한 답변인지
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    /**
     * 유저가 선택한 점수 (1~5)
     */
    @Column(nullable = false)
    private Integer score;
}
