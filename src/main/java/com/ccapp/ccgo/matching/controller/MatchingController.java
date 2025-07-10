package com.ccapp.ccgo.matching.controller;

import com.ccapp.ccgo.question.dto.AnswerRequestDto;
import com.ccapp.ccgo.matching.dto.MatchingResponseDto;
import com.ccapp.ccgo.question.dto.QuestionRequestDto;
import com.ccapp.ccgo.question.dto.QuestionResponseDto;
import com.ccapp.ccgo.question.dto.QuestionUpdateDto;
import com.ccapp.ccgo.matching.service.MatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/matching") // 경로수정필요
public class MatchingController {

    private final MatchingService matchingService;

    /**
     * 팀장이 매칭 시작 버튼 누를 때 호출
     */
//    @PreAuthorize("hasRole('TEAM_LEADER')") 매칭권한
    @PostMapping("/start/{teamId}")
    public MatchingResponseDto startMatching(@PathVariable Long teamId) {
        return matchingService.performMatching(teamId);
    }

    @PostMapping("/answer")
    public void saveAnswers(@RequestBody AnswerRequestDto requestDto) {
        matchingService.saveAnswers(requestDto);
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
}
