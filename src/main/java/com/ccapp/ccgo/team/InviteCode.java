package com.ccapp.ccgo.team;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 초대코드 엔티티
 * - 팀원이 팀에 가입할 때 사용하는 코드 관리
 * - 발급 후 1시간만 유효
 * - 코드 만료 혹은 비활성화 시 하드 삭제
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "invite_code")
public class InviteCode {

    // PK - 초대 코드 (랜덤 문자열, 8자리)
    @Id
    @Column(length = 8)
    private String code;

    // FK - 팀 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    // 초대코드 유효 기간 (생성 후 +1시간)
    private LocalDateTime expiresAt;

    //자동 1시간 설정
    @PrePersist
    public void prePersist() {
        if (this.expiresAt == null) {
            this.expiresAt = LocalDateTime.now().plusHours(1);
        }
    }
    // 코드 유효성 (null 안전성 포함)
    public boolean isExpired() {
        return expiresAt == null || LocalDateTime.now().isAfter(expiresAt);
    }
}
