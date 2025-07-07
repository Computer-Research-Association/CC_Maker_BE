package com.ccapp.ccgo.mission;

import com.ccapp.ccgo.common.MissionStatus;
import com.ccapp.ccgo.mission.repository.MissionTemplateRepository;
import com.ccapp.ccgo.mission.repository.PartnerMissionRepository;
import com.ccapp.ccgo.mission.repository.PartnerRepository;
import com.ccapp.ccgo.mission.repository.UserMissionRepository;
import com.ccapp.ccgo.mission.service.MissionService;
import com.ccapp.ccgo.repository.UserRepository;
import com.ccapp.ccgo.user.User;
import jakarta.transaction.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MissionServiceImpl implements MissionService {

    private final PartnerRepository partnerRepository;
    private final PartnerMissionRepository partnerMissionRepository;
    private final UserMissionRepository userMissionRepository;
    private final MissionTemplateRepository missionTemplateRepository;
    private final UserRepository userRepository;

    @Override
    public PartnerMission refreshPartnerMission(Long userId) {
        // 1. 유저 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        // 2. 유저가 속한 Partner 찾기 (user1 또는 user2인 Partner)
        Partner partner = partnerRepository.findByUser1OrUser2(user, user)
                .orElseThrow(() -> new IllegalStateException("해당 유저의 짝이 없음"));

        // 3. 기존 활성 미션 비활성화 처리
        List<PartnerMission> activeMissions = partnerMissionRepository.findByPartnerAndIsActiveTrue(partner);
        for (PartnerMission pm : activeMissions) {
            pm.setActive(false);
            partnerMissionRepository.save(pm);
        }

        // 1. 마지막 미션 조회
        Optional<PartnerMission> lastMission = partnerMissionRepository.findTopByPartnerOrderByIdDesc(partner);
        Long nextTemplateId = lastMission.map(pm -> pm.getTemplate().getId() + 1).orElse(1L);
        if (nextTemplateId > 50L) nextTemplateId = 1L; // 50개를 순환하면 다시 1로

        // 2. 템플릿 미션 조회
        MissionTemplate template = missionTemplateRepository.findById(nextTemplateId)
                .orElseThrow(() -> new RuntimeException("템플릿 미션 없음"));

        // 3. 템플릿 기반으로 PartnerMission 생성
        PartnerMission newMission = PartnerMission.builder()
                .partner(partner)
                .template(template)  // <-- template를 저장
                .title(template.getTitle())
                .description(template.getDescription())
                .dueDate(LocalDate.now().plusDays(7))
                .isActive(true)
                .build();

        partnerMissionRepository.save(newMission);

        // 5. 기존 UserMission은 유지하거나 삭제 가능 (옵션)
        // 새 UserMission 생성 (짝의 두 유저에게)
        User user1 = partner.getUser1();
        User user2 = partner.getUser2();

        createUserMission(user1, newMission);
        createUserMission(user2, newMission);

        return newMission;
    }

    private void createUserMission(User user, PartnerMission mission) {
        UserMission um = UserMission.builder()
                .user(user)
                .partnerMission(mission)
                .status(MissionStatus.PENDING)
                .build();

        userMissionRepository.save(um);
    }
}