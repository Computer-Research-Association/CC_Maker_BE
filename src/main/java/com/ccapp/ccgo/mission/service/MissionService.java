package com.ccapp.ccgo.mission.service;

import com.ccapp.ccgo.mission.domain.PartnerMission;
import com.ccapp.ccgo.mission.dto.PartnerMissionDto;
import jakarta.transaction.Transactional;

public interface MissionService {

    PartnerMissionDto refreshPartnerMission(Long teamId, Long userId, int score);

}