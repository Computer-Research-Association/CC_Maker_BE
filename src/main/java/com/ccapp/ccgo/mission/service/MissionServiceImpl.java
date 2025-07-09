package com.ccapp.ccgo.mission.service;

import com.ccapp.ccgo.common.MissionStatus;
import com.ccapp.ccgo.mission.domain.*;
import com.ccapp.ccgo.mission.dto.PartnerMissionDto;
import com.ccapp.ccgo.mission.repository.*;
import com.ccapp.ccgo.repository.UserRepository;
import com.ccapp.ccgo.team.Team;
import com.ccapp.ccgo.repository.TeamRepository;
import com.ccapp.ccgo.user.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MissionServiceImpl implements MissionService {

    private final PartnerRepository partnerRepository;
    private final PartnerMissionRepository partnerMissionRepository;
    private final UserMissionRepository userMissionRepository;
    private final MissionTemplateRepository missionTemplateRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;

    @Override
    public PartnerMissionDto refreshPartnerMission(Long teamId, Long userId, int score) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("팀 없음"));

        Partner partner = partnerRepository.findByTeamAndUser1OrTeamAndUser2(team, user, team, user)
                .orElseThrow(() -> new IllegalStateException("해당 팀에서 짝이 없음"));

        // 해당 점수 미션 중 기존 활성 미션 비활성화
        List<PartnerMission> activeMissions = partnerMissionRepository
                .findByPartnerAndIsActiveTrueAndTemplateScore(partner, score);
        for (PartnerMission pm : activeMissions) {
            pm.setActive(false);
            partnerMissionRepository.save(pm);
        }

        // 최근 3개 미션 조회해서 제외할 템플릿 ID 리스트 만들기
        List<PartnerMission> recentMissions = partnerMissionRepository
                .findTop3ByPartnerOrderByDueDateDesc(partner);
        List<Long> excludeIds = recentMissions.stream()
                .map(pm -> pm.getTemplate().getId())
                .collect(Collectors.toList());

        // 점수별 전체 미션 리스트 조회
        List<MissionTemplate> allTemplates = missionTemplateRepository.findByScore(score);

        // 제외 리스트에 없는 미션만 후보로 필터링
        List<MissionTemplate> candidateTemplates = allTemplates.stream()
                .filter(mt -> !excludeIds.contains(mt.getId()))
                .collect(Collectors.toList());

        // 후보가 없으면 전체에서 랜덤 선택
        if (candidateTemplates.isEmpty()) {
            candidateTemplates = allTemplates;
        }

        // 랜덤 미션 선택
        int randomIndex = (int) (Math.random() * candidateTemplates.size());
        MissionTemplate selectedTemplate = candidateTemplates.get(randomIndex);

        PartnerMission newMission = PartnerMission.builder()
                .partner(partner)
                .template(selectedTemplate)
                .title(selectedTemplate.getTitle())
                .description(selectedTemplate.getDescription())
                .dueDate(LocalDate.now().plusDays(14))
                .isActive(true)
                .build();

        partnerMissionRepository.save(newMission);

        createUserMission(partner.getUser1(), newMission);
        createUserMission(partner.getUser2(), newMission);

        return PartnerMissionDto.fromEntity(newMission);
    }

    private void createUserMission(User user, PartnerMission mission) {
        UserMission userMission = UserMission.builder()
                .user(user)
                .partnerMission(mission)
                .status(MissionStatus.PENDING)
                .build();
        userMissionRepository.save(userMission);
    }
}
