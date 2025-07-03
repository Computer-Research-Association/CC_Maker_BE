package com.ccapp.ccgo.repository;

import com.ccapp.ccgo.team.InviteCode;
import com.ccapp.ccgo.team.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * InviteCode 엔티티용 Repository
 */
public interface InviteCodeRepository extends JpaRepository<InviteCode, String> {

    // 코드 존재 여부
    boolean existsByCode(String code);

    // 유효한 초대코드 조회
    Optional<InviteCode> findByCodeAndExpiresAtAfter(String code, LocalDateTime now);

    // 특정 팀의 초대코드 찾기
    Optional<InviteCode> findByTeam_TeamId(Long teamId);

    // 만료 코드 삭제
    void deleteByExpiresAtBefore(LocalDateTime now);

    // 특정 팀의 기존 초대코드 삭제
    void deleteByTeam(Team team);

}
