package com.ccapp.ccgo.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 개인정보 동의서 전문을 저장하는 엔티티
 * - 버전별로 동의서 내용을 관리
 * - 사용자가 어떤 버전의 동의서에 동의했는지 추적 가능
 */
@Entity
@Table(name = "privacy_agreements")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PrivacyAgreement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 동의서 버전 (예: "v1.0", "v1.1")
    @Column(unique = true, nullable = false)
    private String version;

    // 동의서 전문 내용
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    // 시행일
    @Column(nullable = false)
    private LocalDate effectiveDate;

    // 생성일시
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 활성화 여부 (현재 사용 중인 버전인지)
    @Column(nullable = false)
    private boolean isActive = true;

    // 생성 시 자동으로 현재 시간 설정
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
