package com.ccapp.ccgo.mission.repository;
import com.ccapp.ccgo.mission.domain.MissionTemplate;
import com.ccapp.ccgo.mission.domain.Partner;
import com.ccapp.ccgo.mission.domain.PartnerMission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PartnerMissionRepository extends JpaRepository<PartnerMission, Long> {
    // 활성화된 점수별 미션 조회
    List<PartnerMission> findByPartnerAndIsActiveTrueAndTemplateScore(Partner partner, int score);

    // 최근 3개 미션 조회 (dueDate 기준 내림차순)
    List<PartnerMission> findTop3ByPartnerOrderByDueDateDesc(Partner partner);

}