package com.ccapp.ccgo.mission.service;

import com.ccapp.ccgo.mission.PartnerMission;

public interface MissionService {

    // 유저가 새로고침 시 새로운 미션을 짝에 할당
    PartnerMission refreshPartnerMission(Long userId);
}