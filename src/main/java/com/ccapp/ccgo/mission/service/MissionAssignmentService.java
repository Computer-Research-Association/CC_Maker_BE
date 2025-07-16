package com.ccapp.ccgo.mission.service;

import com.ccapp.ccgo.mission.dto.TeamMissionDto;
import com.ccapp.ccgo.mission.entity.MissionTemplate;
import com.ccapp.ccgo.mission.entity.TeamMission;
import com.ccapp.ccgo.mission.repository.MissionTemplateRepository;
import com.ccapp.ccgo.mission.repository.TeamMissionRepository;
import com.ccapp.ccgo.team.entity.Team;
import com.ccapp.ccgo.team.repository.TeamRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MissionAssignmentService {

    private final TeamRepository teamRepository;
    private final MissionTemplateRepository missionTemplateRepository;
    private final TeamMissionRepository teamMissionRepository;

    //팀에게 미션 6개씩 부여
    @Transactional
    public void assignMissionsToTeam(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));

        if (!teamMissionRepository.findByTeam(team).isEmpty()) {
            throw new IllegalStateException("이미 이 팀에는 미션이 부여되었습니다.");
        }

        // 각 학점별로 6개씩 미션 랜덤 선택 및 저장
        assignMissionsByScore(team, 1, 6);
        assignMissionsByScore(team, 3, 6);
        assignMissionsByScore(team, 5, 6);
        assignMissionsByScore(team, 10, 6);
    }

    private void assignMissionsByScore(Team team, int score, int count) {
        List<MissionTemplate> missions = missionTemplateRepository.findByScore(score);

        if (missions.size() < count) {
            throw new IllegalStateException(score + "점 미션이 최소 " + count + "개 이상 필요합니다.");
        }

        // 랜덤으로 섞고 count 만큼 잘라서 사용
        Collections.shuffle(missions);
        List<MissionTemplate> selectedMissions = missions.stream()
                .limit(count)
                .collect(Collectors.toList());

        for (MissionTemplate template : selectedMissions) {
            TeamMission teamMission = new TeamMission(team, template);
            teamMissionRepository.save(teamMission);
        }
    }

    @Transactional(readOnly = true)
    public List<TeamMissionDto> getTeamMissions(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));

        List<TeamMission> teamMissions = teamMissionRepository.findByTeam(team);

        return teamMissions.stream()
                .map(tm -> new TeamMissionDto(
                        tm.getMissionTemplate().getId(),
                        tm.getMissionTemplate().getTitle(),
                        tm.getMissionTemplate().getDescription(),
                        tm.getMissionTemplate().getScore()
                ))
                .collect(Collectors.toList());
    }

}
