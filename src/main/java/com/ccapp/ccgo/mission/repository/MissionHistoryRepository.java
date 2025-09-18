package com.ccapp.ccgo.mission.repository;

import com.ccapp.ccgo.mission.entity.MissionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MissionHistoryRepository extends JpaRepository<MissionHistory, Long> {
    
    // 특정 서브그룹의 미션 히스토리 조회
    List<MissionHistory> findBySubGroup_IdOrderByCompletedAtDesc(Long subGroupId);
    
    // 특정 사용자의 미션 히스토리 조회 (팀별)
    List<MissionHistory> findByUser_IdAndTeam_TeamIdOrderByCompletedAtDesc(Long userId, Long teamId);
    
    // 특정 팀의 미션 히스토리 조회
    @Query("SELECT mh FROM MissionHistory mh WHERE mh.team.teamId = :teamId ORDER BY mh.completedAt DESC")
    List<MissionHistory> findByTeamIdOrderByCompletedAtDesc(@Param("teamId") Long teamId);
    
    // 특정 미션 템플릿의 완료 히스토리 조회
    List<MissionHistory> findByMissionTemplate_IdOrderByCompletedAtDesc(Long missionTemplateId);
    
    // 특정 서브그룹에서 특정 미션 템플릿이 완료되었는지 확인
    boolean existsBySubGroup_IdAndMissionTemplate_Id(Long subGroupId, Long missionTemplateId);
}
