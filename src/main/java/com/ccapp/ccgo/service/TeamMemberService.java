package com.ccapp.ccgo.service;

import com.ccapp.ccgo.repository.TeamMemberRepository;
import com.ccapp.ccgo.team.TeamMember;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeamMemberService {

    private final TeamMemberRepository teamMemberRepository;

    @Transactional
    public void markSurveyCompleted(Long userId, Long teamId) {
        TeamMember teamMember = teamMemberRepository
                .findByUser_IdAndTeam_TeamId(userId, teamId)
                .orElseThrow(() -> new RuntimeException("팀 멤버를 찾을 수 없습니다."));

        teamMember.setSurveyCompleted(true);  // ← 여기서 DB의 isSurveyCompleted를 true로 변경
    }

}
