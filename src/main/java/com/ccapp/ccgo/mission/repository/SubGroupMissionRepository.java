package com.ccapp.ccgo.mission.repository;

import com.ccapp.ccgo.matching.domain.entity.SubGroup;
import com.ccapp.ccgo.mission.entity.SubGroupMission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubGroupMissionRepository extends JpaRepository<SubGroupMission, Long> {

    List<SubGroupMission> findBySubGroup(SubGroup subGroup);

    Optional<SubGroupMission> findBySubGroupAndMissionTemplateId(SubGroup subGroup, Long missionTemplateId);


}
