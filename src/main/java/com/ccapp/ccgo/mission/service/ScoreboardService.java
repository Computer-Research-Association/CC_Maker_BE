package com.ccapp.ccgo.mission.service;

import com.ccapp.ccgo.matching.domain.entity.SubGroup;
import com.ccapp.ccgo.matching.repository.SubGroupMemberRepository;
import com.ccapp.ccgo.matching.repository.SubGroupRepository;
import com.ccapp.ccgo.mission.dto.ScoreboardResponseDto;
import com.ccapp.ccgo.mission.dto.SubGroupScoreDto;
import com.ccapp.ccgo.mission.entity.SubGroupMission;
import com.ccapp.ccgo.mission.repository.SubGroupMissionRepository;
import com.ccapp.ccgo.team.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScoreboardService {

    private final SubGroupMemberRepository subGroupMemberRepository;
    private final SubGroupRepository subGroupRepository;
    private final SubGroupMissionRepository subGroupMissionRepository;
    private final TeamService teamService; // 팀 최소 학점 조회용

    // 현재 유저가 속한 서브그룹 점수 조회
    public SubGroupScoreDto getMySubGroupScore(Long teamId, Long userId) {
        Long subGroupId = subGroupMemberRepository.findSubGroupIdByTeamIdAndUserId(teamId, userId)
                .orElseThrow(() -> new IllegalArgumentException("서브그룹을 찾을 수 없습니다."));

        SubGroup subGroup = subGroupRepository.findById(subGroupId)
                .orElseThrow(() -> new IllegalArgumentException("서브그룹을 찾을 수 없습니다."));

        int score = calculateSubGroupScore(subGroup);

        return new SubGroupScoreDto(subGroup.getId(), subGroup.getName(), score);
    }

    // 팀 내 다른 서브그룹 점수 조회 (내 서브그룹 제외)
    public List<SubGroupScoreDto> getOtherSubGroupScores(Long teamId, Long excludeSubGroupId) {
        List<SubGroup> subGroups = subGroupRepository.findByTeam_TeamId(teamId);

        return subGroups.stream()
                .filter(sg -> !sg.getId().equals(excludeSubGroupId))
                .map(sg -> new SubGroupScoreDto(sg.getId(), sg.getName(), calculateSubGroupScore(sg)))
                .collect(Collectors.toList());
    }

    // 서브그룹 점수 계산 (완료된 미션 점수 합)
    private int calculateSubGroupScore(SubGroup subGroup) {
        List<SubGroupMission> missions = subGroupMissionRepository.findBySubGroup(subGroup);

        return missions.stream()
                .filter(SubGroupMission::isCompleted)
                .mapToInt(m -> m.getMissionTemplate().getScore())
                .sum();
    }

    // 팀 최소 학점 조회
    public int getTeamMinScore(Long teamId) {
        return teamService.getMinScore(teamId);
    }

    // 최종 스코어보드 반환
    public ScoreboardResponseDto getScoreboard(Long teamId, Long userId) {
        int minScore = getTeamMinScore(teamId);
        SubGroupScoreDto mySubGroup = getMySubGroupScore(teamId, userId);
        List<SubGroupScoreDto> otherSubGroups = getOtherSubGroupScores(teamId, mySubGroup.getSubGroupId());

        return new ScoreboardResponseDto(minScore, mySubGroup, otherSubGroups);
    }
}
