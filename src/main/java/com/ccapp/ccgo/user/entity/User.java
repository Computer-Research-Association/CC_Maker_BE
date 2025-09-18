package com.ccapp.ccgo.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 사용자 정보를 담는 엔티티
 * - 시스템 전체 사용자 관리
 * - 팀 소속 여부는 TeamMember 엔티티에서 관리
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

    // PK - 사용자 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 이메일 (회원가입 아이디 역할), UNIQUE
    @Column(nullable = false, unique = true)
    private String email;

    // 비밀번호
    @Column(nullable = false)
    private String password;

    // 사용자 이름
    private String name;

    // 생년월일 (nullable 허용)
    @Column(name = "birthdate")
    private LocalDate birthdate;

    // 성별 (e.g. "MALE", "FEMALE")
    @Column(name = "gender")
    private String gender;

    // 회원 가입 시각
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // 개인정보 동의 관련 필드들
    @Column(name = "privacy_agreement_version")
    private String privacyAgreementVersion;

    @Column(name = "privacy_agreed")
    private boolean privacyAgreed;

    @Column(name = "privacy_agreed_at")
    private LocalDateTime privacyAgreedAt;

    @Column(name = "privacy_agreed_method")
    private String privacyAgreedMethod;

    @Column(name = "privacy_agreed_environment")
    private String privacyAgreedEnvironment;

    // 회원 가입 시 자동으로 현재 시간 설정
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

}


