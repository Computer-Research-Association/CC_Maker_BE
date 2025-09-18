package com.ccapp.ccgo.mission.controller;

import com.ccapp.ccgo.mission.dto.MissionHistoryDto;
import com.ccapp.ccgo.mission.service.SubGroupMissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/mission/history")
@RequiredArgsConstructor
@Slf4j
public class MissionHistoryController {
    
    private final SubGroupMissionService subGroupMissionService;
    
    // 서브그룹의 미션 히스토리 조회
    @GetMapping("/subgroup/{subGroupId}")
    public ResponseEntity<List<MissionHistoryDto>> getMissionHistoryBySubGroup(@PathVariable Long subGroupId) {
        log.info("서브그룹 미션 히스토리 조회 요청: subGroupId = {}", subGroupId);
        List<MissionHistoryDto> histories = subGroupMissionService.getMissionHistoryBySubGroup(subGroupId);
        return ResponseEntity.ok(histories);
    }
    
    // 사용자의 미션 히스토리 조회 (팀별)
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<MissionHistoryDto>> getMissionHistoryByUser(
            @PathVariable Long userId,
            @RequestParam Long teamId) {
        log.info("사용자 미션 히스토리 조회 요청: userId = {}, teamId = {}", userId, teamId);
        try {
            List<MissionHistoryDto> histories = subGroupMissionService.getMissionHistoryByUser(userId, teamId);
            return ResponseEntity.ok(histories);
        } catch (Exception e) {
            log.error("미션 히스토리 조회 중 오류 발생: {}", e.getMessage());
            // 테이블이 존재하지 않는 경우 등 오류 발생 시 빈 리스트 반환
            return ResponseEntity.ok(new ArrayList<>());
        }
    }
    
    // 팀의 미션 히스토리 조회
    @GetMapping("/team/{teamId}")
    public ResponseEntity<List<MissionHistoryDto>> getMissionHistoryByTeam(@PathVariable Long teamId) {
        log.info("팀 미션 히스토리 조회 요청: teamId = {}", teamId);
        List<MissionHistoryDto> histories = subGroupMissionService.getMissionHistoryByTeam(teamId);
        return ResponseEntity.ok(histories);
    }
}
