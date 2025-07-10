package com.ccapp.ccgo.mission.service;

import com.ccapp.ccgo.common.MissionStatus;
import com.ccapp.ccgo.mission.entity.*;
import com.ccapp.ccgo.mission.dto.PartnerMissionDto;
import com.ccapp.ccgo.mission.repository.*;
import com.ccapp.ccgo.user.repository.UserRepository;
import com.ccapp.ccgo.team.entity.Team;
import com.ccapp.ccgo.team.repository.TeamRepository;
import com.ccapp.ccgo.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

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
    public PartnerMissionDto refreshPartnerMission(Long teamId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("팀 없음"));

        Partner partner = partnerRepository.findByTeamAndUser1OrTeamAndUser2(team, user, team, user)
                .orElseThrow(() -> new IllegalStateException("해당 팀에서 짝이 없음"));

        // 기존 활성 미션 비활성화
        List<PartnerMission> activeMissions = partnerMissionRepository.findByPartnerAndIsActiveTrue(partner);
        for (PartnerMission pm : activeMissions) {
            pm.setActive(false);
            partnerMissionRepository.save(pm);
        }

        // 마지막 미션 기반으로 다음 템플릿 선택
        Long nextTemplateId = partnerMissionRepository.findTopByPartnerOrderByIdDesc(partner)
                .map(pm -> pm.getTemplate().getId() + 1)
                .orElse(1L);
        if (nextTemplateId > 50L) nextTemplateId = 1L;

        MissionTemplate template = missionTemplateRepository.findById(nextTemplateId)
                .orElseThrow(() -> new RuntimeException("템플릿 미션 없음"));

        PartnerMission newMission = PartnerMission.builder()
                .partner(partner)
                .template(template)
                .title(template.getTitle())
                .description(template.getDescription())
                .dueDate(LocalDate.now().plusDays(7))
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
