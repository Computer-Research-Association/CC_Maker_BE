package com.ccapp.ccgo.mission.controller;

import com.ccapp.ccgo.mission.dto.PartnerMissionDto;
import com.ccapp.ccgo.mission.service.MissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/missions")
@RequiredArgsConstructor
public class MissionController {

    private final MissionService missionService;

    // 팀 ID와 유저 ID를 기반으로 미션 새로고침
    @PostMapping("/refresh")
    public ResponseEntity<PartnerMissionDto> refreshPartnerMission(
            @RequestParam Long teamId,
            @RequestParam Long userId
    ) {
        PartnerMissionDto partnerMissionDto = missionService.refreshPartnerMission(teamId, userId);
        return ResponseEntity.ok(partnerMissionDto);
    }
}
