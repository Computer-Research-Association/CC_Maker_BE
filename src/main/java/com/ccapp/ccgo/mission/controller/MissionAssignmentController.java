package com.ccapp.ccgo.mission.controller;

import com.ccapp.ccgo.mission.dto.MissionCompleteRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ccapp.ccgo.mission.service.SubGroupMissionService;
import com.ccapp.ccgo.mission.dto.SubGroupMissionDto;

import java.util.List;

@RestController
@RequestMapping("/api/missions")
@RequiredArgsConstructor
public class MissionAssignmentController {

    private final SubGroupMissionService subGroupMissionService;

    // 서브그룹에 미션 부여
    @PostMapping("/assign/subgroup/{subGroupId}")
    public ResponseEntity<String> assignMissionsToSubGroup(@PathVariable Long subGroupId) {
        subGroupMissionService.assignMissionsToSubGroup(subGroupId);
        return ResponseEntity.ok("미션이 서브그룹에 성공적으로 부여되었습니다.");
    }

    // 서브그룹에 부여된 미션 리스트 조회
    @GetMapping("/subgroup/{subGroupId}")
    public ResponseEntity<List<SubGroupMissionDto>> getSubGroupMissions(@PathVariable Long subGroupId) {
        List<SubGroupMissionDto> missions = subGroupMissionService.getMissions(subGroupId);
        return ResponseEntity.ok(missions);
    }

    // 미션 완료 처리
    @PostMapping("/complete")
    public ResponseEntity<String> completeMission(@RequestBody MissionCompleteRequest request) {
        try {
            subGroupMissionService.completeMission(request.getTeamId(), request.getSubGroupId(), request.getMissionId());
            return ResponseEntity.ok("미션 완료 처리 성공");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("미션 완료 처리 실패: " + e.getMessage());
        }
    }


    //미션 새로고침
    @PostMapping("/refresh/subgroup/{subGroupId}/{subGroupMissionId}/{score}")
    public ResponseEntity<String> refreshMission(
            @PathVariable Long subGroupId,
            @PathVariable Long subGroupMissionId,
            @PathVariable Integer score) {
        try {
            System.out.println("진입합니당." + subGroupId + subGroupMissionId + score);
            subGroupMissionService.refreshSingleMission(subGroupId, subGroupMissionId, score);
            return ResponseEntity.ok("미션 새로고침 완료");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("미션 새로고침 실패: " + e.getMessage());
        }
    }

}
