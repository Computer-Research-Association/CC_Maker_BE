package com.ccapp.ccgo.repository;

import com.ccapp.ccgo.team.TeamMember;
import com.ccapp.ccgo.user.User;
import com.ccapp.ccgo.team.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * TeamMember 엔티티용 Repository
 */
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    // 한 유저가 이미 어떤 팀에 속해있는지 검사
    boolean existsByUser(User user);

    // 현재 소속 중인 팀 찾기 (Soft Delete 고려)
    Optional<TeamMember> findByUserAndIsActiveTrue(User user);

    //teammember에서 유저 조회
    Optional<TeamMember> findByUser(User user);

    // 팀별 멤버 목록
    List<TeamMember> findAllByTeamAndIsActiveTrue(Team team);

    // 이미 특정 유저가 특정 팀 인지 확인
    boolean existsByUserAndTeam(User user, Team team);
}
