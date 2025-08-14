package com.ccapp.ccgo.matching.controller;

import com.ccapp.ccgo.auth.jwt.LoginUserDetails;
import com.ccapp.ccgo.matching.dto.MatchedNamesResponse;
import com.ccapp.ccgo.matching.repository.SubGroupMemberRepository;
import com.ccapp.ccgo.question.dto.AnswerRequestDto;
import com.ccapp.ccgo.matching.dto.MatchingResponseDto;
import com.ccapp.ccgo.question.dto.QuestionRequestDto;
import com.ccapp.ccgo.question.dto.QuestionResponseDto;
import com.ccapp.ccgo.question.dto.QuestionUpdateDto;
import com.ccapp.ccgo.matching.service.MatchingService;
import com.ccapp.ccgo.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/matching")
@Slf4j
public class MatchingController {

    private final MatchingService matchingService;
    private final SubGroupMemberRepository subGroupMemberRepository;

    /**
     * 팀 매칭 시작
     * 팀장이 매칭 시작 버튼을 누를 때 호출
     */
    @PostMapping("/start/{teamId}")
    public ResponseEntity<MatchingResponseDto> startMatching(@PathVariable Long teamId) {
        log.info("[Matching] 매칭 시작 요청 | teamId: {}", teamId);
        return ResponseEntity.ok(matchingService.performMatching(teamId));
    }

    /**
     * 설문 답변 저장
     */
    @PostMapping("/answer")
    public ResponseEntity<Void> saveAnswers(@RequestBody AnswerRequestDto dto,
                                        @AuthenticationPrincipal LoginUserDetails loginUserDetails) {
        User currentUser = loginUserDetails.getUser();
        matchingService.saveAnswers(dto, currentUser);
        return ResponseEntity.ok().build();
    }

    /**
     * 설문 질문 생성
     */
    @PostMapping("/question")
    public void createQuestions(@RequestBody QuestionRequestDto requestDto) {
        matchingService.createQuestions(requestDto);
    }

    /**
     * 팀별 설문 질문 조회
     */
    @GetMapping("/question")
    public List<QuestionResponseDto> getQuestions(@RequestParam Long teamId) {
        return matchingService.getQuestions(teamId);
    }

    /**
     * 설문 질문 수정
     */
    @PutMapping("/question/{questionId}")
    public void updateQuestion(@PathVariable Long questionId,
                               @RequestBody QuestionUpdateDto dto) {
        matchingService.updateQuestion(questionId, dto);
    }

    /**
     * 설문 질문 삭제
     */
    @DeleteMapping("/question/{questionId}")
    public void deleteQuestion(@PathVariable Long questionId) {
        matchingService.deleteQuestion(questionId);
    }

    /**
     * 매칭된 팀원 이름 조회
     * 매칭 완료 후 해당 사용자의 서브그룹 멤버들을 조회
     */
    @GetMapping("/matched-names/{teamId}")
    public ResponseEntity<MatchedNamesResponse> getMatchedNames(
            @RequestParam Long userId,
            @PathVariable Long teamId) {

        log.info("[Matching] 매칭된 팀원 조회 | userId: {}, teamId: {}", userId, teamId);

        // 1) userId와 teamId로 subGroupId 조회
        Long subGroupId = matchingService.findSubGroupIdByTeamIdAndUserId(teamId, userId);

        if (subGroupId == null) {
            // 서브그룹 미존재 (매칭 안된 상태)
            return ResponseEntity.status(404).body(new MatchedNamesResponse(teamId, null, List.of()));
        }

        // 2) 매칭된 멤버 이름 조회 (본인 제외)
        List<String> matchedNames = matchingService.getMatchedUserNames(userId, teamId);

        log.info("[Matching] 매칭된 팀원 수: {}", matchedNames.size());

        MatchedNamesResponse response = new MatchedNamesResponse(teamId, subGroupId, matchedNames);

        return ResponseEntity.ok(response);
    }

    /**
     * 사용자의 서브그룹 ID 조회
     * 매칭 완료 후 사용자가 속한 서브그룹을 확인
     */
    @GetMapping("/subgroup/{teamId}")
    public ResponseEntity<Map<String, Object>> getSubGroupIdByTeamId(
            @PathVariable Long teamId,
            @RequestParam Long userId) {
        Optional<Long> subGroupId = subGroupMemberRepository.findSubGroupIdByTeamIdAndUserId(teamId, userId);
        Map<String, Object> response = new HashMap<>();
        response.put("subGroupId", subGroupId.orElse(null));
        return ResponseEntity.ok(response);
    }

}







