package com.ccapp.ccgo.mission.controller;

import com.ccapp.ccgo.mission.dto.TeamMissionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ccapp.ccgo.mission.service.MissionAssignmentService;

import java.util.List;

@RestController
@RequestMapping("/api/missions")
@RequiredArgsConstructor
public class MissionAssignmentController {

    private final MissionAssignmentService missionAssignmentService;

    @PostMapping("/assign/{teamId}")
    public ResponseEntity<String> assignMissionsToTeam(@PathVariable Long teamId) {
        missionAssignmentService.assignMissionsToTeam(teamId);
        return ResponseEntity.ok("미션이 팀에 성공적으로 부여되었습니다.");
    }

    // 팀에 부여된 미션 리스트 조회 API
    @GetMapping("/team/{teamId}")
    public ResponseEntity<List<TeamMissionDto>> getTeamMissions(@PathVariable Long teamId) {
        List<TeamMissionDto> missions = missionAssignmentService.getTeamMissions(teamId);
        return ResponseEntity.ok(missions);
    }
}
