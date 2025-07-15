package com.ccapp.ccgo.mission.repository;

import com.ccapp.ccgo.mission.entity.MissionTemplate;
import com.ccapp.ccgo.mission.entity.TeamMission;
import com.ccapp.ccgo.team.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamMissionRepository extends JpaRepository<TeamMission, Long> {

    // 특정 팀에 할당된 미션 리스트 조회
    List<TeamMission> findByTeam(Team team);



}
