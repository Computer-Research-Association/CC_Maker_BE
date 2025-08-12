package com.ccapp.ccgo.mission.repository;

import com.ccapp.ccgo.matching.domain.entity.SubGroup;
import com.ccapp.ccgo.mission.entity.SubGroupMission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubGroupMissionRepository extends JpaRepository<SubGroupMission, Long> {

    List<SubGroupMission> findBySubGroup(SubGroup subGroup);

    Optional<SubGroupMission> findBySubGroupAndMissionTemplateId(SubGroup subGroup, Long missionTemplateId);

    // 사용자가 이미 미션을 받았는지 체크하는 메서드
    boolean existsBySubGroup_Team_TeamIdAndSubGroup_SubGroupMembers_User_Id(Long teamId, Long userId);

}
