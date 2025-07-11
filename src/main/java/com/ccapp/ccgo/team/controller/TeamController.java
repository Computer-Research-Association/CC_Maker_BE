package com.ccapp.ccgo.controller;
import com.ccapp.ccgo.dto.SurveyCompleteRequest;
import com.ccapp.ccgo.dto.TeamResponseDto;
import com.ccapp.ccgo.service.TeamMemberService;
import com.ccapp.ccgo.user.User;
import com.ccapp.ccgo.jwt.LoginUserDetails;
import com.ccapp.ccgo.repository.TeamMemberRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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


}