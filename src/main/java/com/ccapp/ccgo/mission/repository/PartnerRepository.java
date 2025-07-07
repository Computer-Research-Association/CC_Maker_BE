package com.ccapp.ccgo.mission.repository;

import com.ccapp.ccgo.mission.Partner;
import com.ccapp.ccgo.team.Team;
import com.ccapp.ccgo.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PartnerRepository extends JpaRepository<Partner, Long> {
    // 추가로 필요한 커스텀 메서드 작성 가능
    boolean existsByTeamAndUser1AndUser2(Team team, User user1, User user2);

    List<Partner> findByTeamId(Long teamId);

    Optional<Partner> findByUser1OrUser2(User user1, User user2);

}