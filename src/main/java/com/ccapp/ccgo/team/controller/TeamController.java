package com.ccapp.ccgo.team.controller;
import com.ccapp.ccgo.auth.jwt.LoginUserDetails;


import com.ccapp.ccgo.question.dto.SurveyCompleteRequest;
import com.ccapp.ccgo.service.TeamMemberService;
import com.ccapp.ccgo.team.dto.TeamResponseDto;
import com.ccapp.ccgo.team.repository.TeamMemberRepository;
import com.ccapp.ccgo.user.entity.User;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/team")
public class TeamController {

    private final TeamMemberService teamMemberService;
    private final TeamMemberRepository teamMemberRepository;

    @GetMapping("/mine")
    public ResponseEntity<List<TeamResponseDto>> getMyTeams(
            @AuthenticationPrincipal LoginUserDetails userDetails) {

        User user = userDetails.getUser();

        List<TeamResponseDto> result = teamMemberRepository
                .findAllByUserAndIsActiveTrue(user)
                .stream()
                .map(tm -> new TeamResponseDto(
                        tm.getTeam().getTeamId(),   // getTeamId()로 변경
                        tm.getTeam().getTeamName()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }


    @PostMapping("/survey/complete")
    public ResponseEntity<Void> completeSurvey(@RequestBody SurveyCompleteRequest request,
                                               @AuthenticationPrincipal LoginUserDetails loginUserDetails) {
        System.out.print("프로그램ㅅ ㅣ작");
        User currentUser = loginUserDetails.getUser();
        System.out.print("1");
        teamMemberService.markSurveyCompleted(currentUser.getId(), request.getTeamId());
        System.out.print("2");
        return ResponseEntity.ok().build();
    }


    @GetMapping("/{teamId}/survey-status")
    public ResponseEntity<?> getSurveyStatus(
            @PathVariable Long teamId,
            @AuthenticationPrincipal LoginUserDetails loginUserDetails) {

        User currentUser = loginUserDetails.getUser();

        boolean isCompleted = teamMemberService.isSurveyCompleted(currentUser.getId(), teamId);

        return ResponseEntity.ok().body(Map.of("issurveycompleted", isCompleted));  //map을 써서 보낼지 dto로 보낼지
        //gpt왈 map이 더 빠르고 간단함 ㅇㅇ 흠....
    }


}