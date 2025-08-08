package com.ccapp.ccgo.team.service;


import com.ccapp.ccgo.team.dto.SurveyStatusDto;
import com.ccapp.ccgo.team.dto.TeamMatchingStatusDto;
import com.ccapp.ccgo.team.dto.TeamResponseDto;
import com.ccapp.ccgo.team.entity.Team;
import com.ccapp.ccgo.team.entity.TeamMember;
import com.ccapp.ccgo.team.repository.TeamMemberRepository;
import com.ccapp.ccgo.team.repository.TeamRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamMemberService {

    private final TeamMemberRepository teamMemberRepository;
    private final TeamRepository teamRepository;

    @Transactional
    public void markSurveyCompleted(Long userId, Long teamId) {
        TeamMember teamMember = teamMemberRepository
                .findByUser_IdAndTeam_TeamId(userId, teamId)
                .orElseThrow(() -> new RuntimeException("팀 멤버를 찾을 수 없습니다."));

        teamMember.setSurveyCompleted(true);  // ← 여기서 DB의 isSurveyCompleted를 true로 변경
    }

    @Transactional
    public boolean isSurveyCompleted(Long userId, Long teamId) {
        TeamMember teamMember = teamMemberRepository
                .findByUser_IdAndTeam_TeamId(userId, teamId)
                .orElseThrow(() -> new RuntimeException("팀 멤버를 찾을 수 없습니다."));
        return teamMember.isSurveyCompleted();  // ← DB 필드 반환
    }


    @Transactional(readOnly = true)
    public List<SurveyStatusDto> getAllSurveyStatus(Long teamId) {
        List<TeamMember> members = teamMemberRepository.findByTeam_TeamIdAndIsActiveTrue(teamId);

        return members.stream()
                .map(member -> new SurveyStatusDto(
                        member.getUser().getId(),
                        member.getUser().getName(),
                        member.isSurveyCompleted()
                ))
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public TeamMatchingStatusDto getTeamInfo(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));
        return new TeamMatchingStatusDto(team.getTeamId(), team.getTeamName(), team.isMatchingStarted());
    }


}
