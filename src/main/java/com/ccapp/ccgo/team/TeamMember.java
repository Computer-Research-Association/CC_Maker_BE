package com.ccapp.ccgo.team;

import com.ccapp.ccgo.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 팀 멤버 엔티티
 * - 어떤 유저가 어떤 팀에 소속되어 있는지 관리
 * - 팀 내 역할(팀장/팀원)도 여기서 관리
 * - 한 유저는 하나의 팀에만 소속될 수 있음
 * - 탈퇴 시 Soft Delete (isActive = false)
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "team_member",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "user_id")
        }
)
public class TeamMember {

    // PK - 팀 멤버 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK - 팀 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    // FK - 유저 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 팀 내 역할 ("TEAM_LEADER" or "TEAM_MEMBER")
    @Column(nullable = false)
    private String role;

    // 팀 가입 일시
    private LocalDateTime joinedAt;

    // Soft Delete 여부 (true = 소속 중, false = 탈퇴)
    @Column(nullable = false)
    private boolean isActive;
}
