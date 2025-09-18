package com.ccapp.ccgo.mission.repository;

import com.ccapp.ccgo.matching.domain.entity.SubGroup;
import com.ccapp.ccgo.mission.entity.SubGroupMission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubGroupMissionRepository extends JpaRepository<SubGroupMission, Long> {

    List<SubGroupMission> findBySubGroup(SubGroup subGroup);

    Optional<SubGroupMission> findBySubGroupAndMissionTemplateId(SubGroup subGroup, Long missionTemplateId);

    // 특정 팀에서 특정 사용자가 이미 미션을 받았는지 확인
    @Query("SELECT COUNT(sgm) > 0 FROM SubGroupMission sgm " +
           "JOIN sgm.subGroup sg " +
           "JOIN sg.team t " +
           "JOIN SubGroupMember sgm2 ON sgm2.subGroup = sg " +
           "WHERE t.teamId = :teamId AND sgm2.user.id = :userId")
    boolean existsByTeamIdAndUserId(@Param("teamId") Long teamId, @Param("userId") Long userId);

}
