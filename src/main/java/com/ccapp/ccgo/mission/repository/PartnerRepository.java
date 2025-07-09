package com.ccapp.ccgo.mission.repository;

import com.ccapp.ccgo.mission.domain.Partner;
import com.ccapp.ccgo.team.Team;
import com.ccapp.ccgo.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PartnerRepository extends JpaRepository<Partner, Long> {

    boolean existsByTeamAndUser1AndUser2(Team team, User user1, User user2);

    List<Partner> findByTeam_TeamId(Long teamId);

    // ❗ 유저1 또는 유저2가 user이고, 특정 팀에 속한 Partner를 찾기
    Optional<Partner> findByTeamAndUser1OrTeamAndUser2(Team team1, User user1, Team team2, User user2);
}
