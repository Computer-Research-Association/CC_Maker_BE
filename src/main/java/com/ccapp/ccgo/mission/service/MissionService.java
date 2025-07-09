package com.ccapp.ccgo.mission.service;

import com.ccapp.ccgo.mission.domain.PartnerMission;
import com.ccapp.ccgo.mission.dto.PartnerMissionDto;
import jakarta.transaction.Transactional;

public interface MissionService {

    // 특정 팀 내에서 유저의 파트너 미션 갱신
    PartnerMissionDto refreshPartnerMission(Long teamId, Long userId);
}