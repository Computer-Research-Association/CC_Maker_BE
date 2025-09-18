package com.ccapp.ccgo.team.repository;

import com.ccapp.ccgo.team.entity.TeamMember;
import com.ccapp.ccgo.user.entity.User;
import com.ccapp.ccgo.team.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * TeamMember 엔티티용 Repository
 */
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    Optional<TeamMember> findByUser_IdAndTeam_TeamId(Long userId, Long teamId);


    // 한 유저가 이미 어떤 팀에 속해있는지 검사
    boolean existsByUser(User user);

    // 현재 소속 중인 팀 찾기 (Soft Delete 고려)
    List<TeamMember> findByUserAndIsActiveTrue(User user);

    //teammember에서 유저 조회
    Optional<TeamMember> findByUser(User user);

    // 유저(user)에 대해 isActive가 true인 TeamMember 리스트 반환
    List<TeamMember> findAllByUserAndIsActiveTrue(User user);


    // 팀별 멤버 목록
    List<TeamMember> findAllByTeamAndIsActiveTrue(Team team);

    // 이미 특정 유저가 특정 팀 인지 확인
    boolean existsByUserAndTeam(User user, Team team);

    List<TeamMember> findByTeam_TeamIdAndIsActiveTrue(Long teamId);

    List<TeamMember> findByUserAndTeamAndIsActiveTrue(User user, Team team);

    boolean existsByUser_IdAndTeam_TeamIdAndIsActiveTrue(Long userId, Long teamId);

}
