package com.ccapp.ccgo;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")  // DB 테이블 이름
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA가 사용할 기본 생성자
@AllArgsConstructor
@Builder
public class User {

    // 유저 아이디
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // PK 자동으로 1씩 증가
    private Long id;

    // 유저 이메일 (중복 불가)
    @Column(nullable = false, unique = true)
    private String email;

    // 유저 패스워드
    @Column(nullable = false)
    private String password;

    // 유저 이름
    private String name;

    // 데이터 입력받은 시간 저장
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    // 저장되기 직전에 createdAt을 자동으로 현재 시간으로 세팅
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}

