package com.ccapp.ccgo.team.service;

import com.ccapp.ccgo.team.entity.Team;
import com.ccapp.ccgo.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;

    /**
     * 팀 최소 학점 업데이트
     */
    @Transactional
    public void updateMinScore(Long teamId, Long userId, Integer minScore) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));

        // 팀장 권한 확인
        if (!team.getCreatedBy().equals(userId)) {
            throw new IllegalArgumentException("팀장만 최소 학점을 설정할 수 있습니다.");
        }

        team.setMinScore(minScore);
        teamRepository.save(team);
    }

    /**
     * 팀 최소 학점 조회
     */
    @Transactional(readOnly = true)
    public Integer getMinScore(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));
        return team.getMinScore();
    }
}
