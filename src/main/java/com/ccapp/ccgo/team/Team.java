package com.ccapp.ccgo.team;

import jakarta.persistence.*;
import lombok.*;

/**
 * 팀 엔티티
 * - 팀 정보를 관리
 * - 한 유저는 오직 하나의 팀만 생성 가능
 * - 팀장 정보는 createdBy 로 관리
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "team",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "createdBy")
        }
)
public class Team {

    // PK - 팀 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long teamId;

    // 팀 이름
    @Column(nullable = false)
    private String teamName;

    // 팀장 유저 ID (User.id 참조), UNIQUE
    @Column(nullable = false, unique = true)
    private Long createdBy;
}
