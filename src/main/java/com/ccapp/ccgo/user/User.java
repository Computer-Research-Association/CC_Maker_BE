package com.ccapp.ccgo.user;

import com.ccapp.ccgo.common.Role;
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

    //팀내 역할 (팀장/팀원)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // 회원 가입 시 자동으로 현재 시간 설정
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    //관심사 설문조사 완료여부
    @Column(name = "is_interest_completed", nullable = false)
    private boolean isInterestCompleted = false;
}


