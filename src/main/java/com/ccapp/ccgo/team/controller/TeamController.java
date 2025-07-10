package com.ccapp.ccgo.team.controller;
import com.ccapp.ccgo.team.dto.TeamResponseDto;
import com.ccapp.ccgo.user.entity.User;
import com.ccapp.ccgo.auth.jwt.LoginUserDetails;
import com.ccapp.ccgo.team.repository.TeamMemberRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/team")
public class TeamController {

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
}