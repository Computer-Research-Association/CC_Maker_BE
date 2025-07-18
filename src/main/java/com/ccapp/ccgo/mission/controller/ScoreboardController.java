package com.ccapp.ccgo.mission.controller;

import com.ccapp.ccgo.mission.dto.ScoreboardResponseDto;
import com.ccapp.ccgo.mission.service.ScoreboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teams/{teamId}/scoreboard")
@RequiredArgsConstructor
public class ScoreboardController {

    private final ScoreboardService scoreboardService;

    /**
     * 특정 팀의 스코어보드를 조회합니다.
     * @param teamId 팀 ID (경로 변수)
     * @param userId 사용자 ID (쿼리 파라미터 혹은 인증 토큰에서 추출 가능)
     * @return 스코어보드 정보
     */
    @GetMapping
    public ResponseEntity<ScoreboardResponseDto> getScoreboard(
            @PathVariable Long teamId,
            @RequestParam Long userId
    ) {
        ScoreboardResponseDto response = scoreboardService.getScoreboard(teamId, userId);
        return ResponseEntity.ok(response);
    }
}
