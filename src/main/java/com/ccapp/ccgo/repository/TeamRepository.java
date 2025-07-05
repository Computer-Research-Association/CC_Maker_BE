package com.ccapp.ccgo.repository;

import com.ccapp.ccgo.team.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Team 엔티티용 Repository
 */
public interface TeamRepository extends JpaRepository<Team, Long> {

    // 팀 ID로 조회
    Optional<Team> findByTeamId(Long teamId);

    // 유저 ID (팀장)로 팀 찾기
    List<Team> findAllByCreatedBy(Long createdBy);

    // 팀 이름 중복 방지
    boolean existsByTeamName(String teamName);

}
