package com.ccapp.ccgo.repository;

import com.ccapp.ccgo.team.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Team 엔티티용 Repository
 */
public interface TeamRepository extends JpaRepository<Team, Long> {

    // 특정 유저가 이미 팀을 만들었는지 확인
    boolean existsByCreatedBy(Long createdBy);

    // 팀 ID로 조회
    Optional<Team> findByTeamId(Long teamId);

    // 팀 이름 중복 방지
    boolean existsByTeamName(String teamName);

}
