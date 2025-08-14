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
@RequestMapping("/api/matching") // 경로수정필요
@Slf4j
public class MatchingController {

    private final MatchingService matchingService;
    private final SubGroupMemberRepository subGroupMemberRepository; // ✅ 주입

    /**
     * 팀장이 매칭 시작 버튼 누를 때 호출
     */
//    @PreAuthorize("hasRole('TEAM_LEADER')") 매칭권한
    //이런식으로 ResponseEntity를 써야 한다.
    @PostMapping("/start/{teamId}")
    public ResponseEntity<MatchingResponseDto> startMatching(@PathVariable Long teamId) {
        log.info("[Matching Start] 요청받음 | teamId: {}", teamId);
        return ResponseEntity.ok(matchingService.performMatching(teamId));
    }

    @PostMapping("/answer")
        public ResponseEntity<Void> saveAnswers(@RequestBody AnswerRequestDto dto,
                                            @AuthenticationPrincipal LoginUserDetails loginUserDetails) {
        User currentUser = loginUserDetails.getUser();
        matchingService.saveAnswers(dto, currentUser);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/question")
    public void createQuestions(@RequestBody QuestionRequestDto requestDto) {
        matchingService.createQuestions(requestDto);
    }

    @GetMapping("/question")
    public List<QuestionResponseDto> getQuestions(@RequestParam Long teamId) {
        return matchingService.getQuestions(teamId);
    }

    @PutMapping("/question/{questionId}")
    public void updateQuestion(@PathVariable Long questionId,
                               @RequestBody QuestionUpdateDto dto) {
        matchingService.updateQuestion(questionId, dto);
    }

    @DeleteMapping("/question/{questionId}")
    public void deleteQuestion(@PathVariable Long questionId) {
        matchingService.deleteQuestion(questionId);
    }



    //왜 안되냐
//    @GetMapping("/matched-names/{teamId}/{subGroupId}")
//    public ResponseEntity<MatchedNamesResponse> getMatchedNames(@AuthenticationPrincipal LoginUserDetails userDetails,
//                                                                @PathVariable Long teamId,
//                                                                @PathVariable Long subGroupId) {
//        List<String> matchedNames = matchingService.getMatchedUserNames(userDetails.getUser().getId(), teamId, subGroupId);
//        MatchedNamesResponse response = new MatchedNamesResponse(teamId, subGroupId, matchedNames);
//        return ResponseEntity.ok(response);
//    }

//    //매칭된 이후, 팀id랑 subgroupid로 팀원 찾아오는놈
//    //이거 authenticaiton 아직 구현 안되서 그런듯
//    @GetMapping("/matched-names/{teamId}/{subGroupId}")
//    public ResponseEntity<MatchedNamesResponse> getMatchedNames(@AuthenticationPrincipal LoginUserDetails userDetails,
//                                                                @PathVariable Long teamId,
//                                                                @PathVariable Long subGroupId) {
//        try {
//            Long userId = userDetails.getUser().getId();
//            System.out.println("[getMatchedNames] userId: " + userId + ", teamId: " + teamId + ", subGroupId: " + subGroupId);
//
//            List<String> matchedNames = matchingService.getMatchedUserNames(userId, teamId, subGroupId);
//
//            System.out.println("[getMatchedNames] matchedNames size: " + (matchedNames != null ? matchedNames.size() : "null"));
//
//            MatchedNamesResponse response = new MatchedNamesResponse(teamId, subGroupId, matchedNames);
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            System.err.println("[getMatchedNames] 서버 에러 발생: " + e.getMessage());
//            e.printStackTrace();
//            return ResponseEntity.status(500).build();
//        }
//    }

    //매칭되고 난 뒤 쓰이는 놈일텐데..
    @GetMapping("/matched-names/{teamId}")
    public ResponseEntity<MatchedNamesResponse> getMatchedNames(
            @RequestParam Long userId,
            @PathVariable Long teamId) {

        System.out.printf("[getMatchedNames] userId: %d, teamId: %d%n", userId, teamId);

        // 1) userId와 teamId로 subGroupId 조회
        Long subGroupId = matchingService.findSubGroupIdByTeamIdAndUserId(teamId, userId);

        if (subGroupId == null) {
            // 서브그룹 미존재 (매칭 안된 상태)
            return ResponseEntity.status(404).body(new MatchedNamesResponse(teamId, null, List.of()));
        }

        // 2) 매칭된 멤버 이름 조회 (본인 제외)
        List<String> matchedNames = matchingService.getMatchedUserNames(userId, teamId);

        System.out.printf("[getMatchedNames] matchedNames size: %d%n", matchedNames.size());

        MatchedNamesResponse response = new MatchedNamesResponse(teamId, subGroupId, matchedNames);

        return ResponseEntity.ok(response);
    }

     //매칭된 직후 작동하는놈
     //현재 임시로 userid 받아오는중인데, 나중에 jwt로 수정 필요.
     @GetMapping("/subgroup/{teamId}")
     public ResponseEntity<Map<String, Object>> getSubGroupIdByTeamId(
             @PathVariable Long teamId,
             @RequestParam Long userId) {
         Optional<Long> subGroupId = subGroupMemberRepository.findSubGroupIdByTeamIdAndUserId(teamId, userId);
         Map<String, Object> response = new HashMap<>();
         response.put("subGroupId", subGroupId.orElse(null)); // null 허용 가능
         return ResponseEntity.ok(response);
     }

}







