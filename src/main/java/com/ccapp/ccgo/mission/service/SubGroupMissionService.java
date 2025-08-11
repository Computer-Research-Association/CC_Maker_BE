package com.ccapp.ccgo.mission.service;

import com.ccapp.ccgo.matching.domain.entity.SubGroup;
import com.ccapp.ccgo.matching.repository.SubGroupRepository;
import com.ccapp.ccgo.mission.dto.ScoreboardResponseDto;
import com.ccapp.ccgo.mission.dto.SubGroupMissionDto;
import com.ccapp.ccgo.mission.dto.SubGroupScoreDto;
import com.ccapp.ccgo.mission.entity.MissionTemplate;
import com.ccapp.ccgo.mission.entity.SubGroupMission;
import com.ccapp.ccgo.mission.repository.MissionTemplateRepository;
import com.ccapp.ccgo.mission.repository.SubGroupMissionRepository;
import lombok.RequiredArgsConstructor;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubGroupMissionService {

    private final SubGroupRepository subGroupRepository;
    private final MissionTemplateRepository missionTemplateRepository;
    private final SubGroupMissionRepository subGroupMissionRepository;

    // 서브그룹에 미션 부여
    @Transactional
    public void assignMissionsToSubGroup(Long subGroupId) {
        SubGroup subGroup = subGroupRepository.findById(subGroupId)
                .orElseThrow(() -> new IllegalArgumentException("서브그룹을 찾을 수 없습니다."));

        if (!subGroupMissionRepository.findBySubGroup(subGroup).isEmpty()) {
            throw new IllegalStateException("이미 이 서브그룹에 미션이 부여되어 있습니다.");
        }

        assignMissionsByScore(subGroup, 1, 6);
        assignMissionsByScore(subGroup, 3, 6);
        assignMissionsByScore(subGroup, 5, 6);
        assignMissionsByScore(subGroup, 10, 6);
    }

    private void assignMissionsByScore(SubGroup subGroup, Integer score, int count) {
        // 1. 이미 할당된 미션 Template ID 목록 조회
        List<Long> existingMissionTemplateIds = subGroupMissionRepository.findBySubGroup(subGroup).stream()
                .map(m -> m.getMissionTemplate().getId())
                .toList();

        // 2. score 조건에 맞는 미션 템플릿 중 기존 할당 미션 제외
        List<MissionTemplate> missions = missionTemplateRepository.findByScore(score).stream()
                .filter(m -> !existingMissionTemplateIds.contains(m.getId()))
                .toList();

        // 3. 미션 개수 체크
        if (missions.size() < count) {
            throw new IllegalStateException(score + "점 미션이 최소 " + count + "개 이상 필요합니다.");
        }

        // 4. 랜덤 섞고 필요한 개수만큼 선택
        Collections.shuffle(missions);
        List<MissionTemplate> selected = missions.subList(0, count);

        // 5. 새 미션 할당
        for (MissionTemplate missionTemplate : selected) {
            SubGroupMission mission = SubGroupMission.builder()
                    .subGroup(subGroup)
                    .missionTemplate(missionTemplate)
                    .completed(false)
                    .build();
            subGroupMissionRepository.save(mission);
        }
    }


    // 미션 완료 처리
    @Transactional
    public void completeMission(Long subGroupMissionId) {
        SubGroupMission mission = subGroupMissionRepository.findById(subGroupMissionId)
                .orElseThrow(() -> new IllegalArgumentException("미션을 찾을 수 없습니다."));
        mission.setCompleted(true);
    }




    // 서브그룹 미션 조회
    @Transactional(readOnly = true)
    public List<SubGroupMissionDto> getMissions(Long subGroupId) {
        SubGroup subGroup = subGroupRepository.findById(subGroupId)
                .orElseThrow(() -> new IllegalArgumentException("서브그룹을 찾을 수 없습니다."));

        List<SubGroupMission> missions = subGroupMissionRepository.findBySubGroup(subGroup);

        return missions.stream()
                .map(m -> new SubGroupMissionDto(
                        m.getId(),
                        m.getMissionTemplate().getId(),
                        m.getMissionTemplate().getTitle(),
                        m.getMissionTemplate().getDescription(),
                        m.getMissionTemplate().getScore(),
                        m.isCompleted()
                ))
                .toList();
    }

    // 미션 새로고침 (완료 안 된 미션 삭제 후 새 할당)
    @Transactional
    public void refreshSingleMission(Long subGroupId, Long subGroupMissionId, Integer score) {
        System.out.println("[refreshSingleMission] 호출됨 - subGroupId: " + subGroupId + ", subGroupMissionId: " + subGroupMissionId + ", score: " + score);

    // 1. 서브그룹 조회
    SubGroup subGroup = subGroupRepository.findById(subGroupId)
            .orElseThrow(() -> {
                System.out.println("[refreshSingleMission] 서브그룹을 찾을 수 없습니다.");
                return new IllegalArgumentException("서브그룹을 찾을 수 없습니다.");
            });

    // 2. 교체할 기존 미션 조회
    SubGroupMission oldMission = subGroupMissionRepository.findById(subGroupMissionId)
            .orElseThrow(() -> {
                System.out.println("[refreshSingleMission] 교체할 미션을 찾을 수 없습니다.");
                return new IllegalArgumentException("교체할 미션을 찾을 수 없습니다.");
            });

    if (!oldMission.getSubGroup().getId().equals(subGroupId)) {
        System.out.println("[refreshSingleMission] 해당 미션이 서브그룹에 속하지 않습니다.");
        throw new IllegalArgumentException("해당 미션이 서브그룹에 속하지 않습니다.");
    }

    // 3. 현재 서브그룹에 할당된 동일 학점 미션 ID
    List<Long> assignedMissionTemplateIds = subGroupMissionRepository.findBySubGroup(subGroup).stream()
            .filter(m -> m.getMissionTemplate().getScore().equals(score))
            .map(m -> m.getMissionTemplate().getId())
            .toList();

    System.out.println("[refreshSingleMission] 현재 서브그룹에 할당된 동일 학점 미션 ID 목록: " + assignedMissionTemplateIds);

    // 4. 교체 후보 미션 (현재 미션 제외) - 가변 리스트로 변환
    List<MissionTemplate> candidates = missionTemplateRepository.findByScore(score).stream()
            .filter(mt -> !assignedMissionTemplateIds.contains(mt.getId())) // 이미 할당된 것 제외
            .filter(mt -> !mt.getId().equals(oldMission.getMissionTemplate().getId())) // 기존 미션 제외
            .collect(Collectors.toList());

    System.out.println("[refreshSingleMission] 교체 후보 미션 수: " + candidates.size());

    if (candidates.isEmpty()) {
        throw new IllegalStateException("교체 가능한 미션이 없습니다.");
    }

    // 5. 랜덤으로 새로운 미션 선택
    int randomIndex = ThreadLocalRandom.current().nextInt(candidates.size());
    MissionTemplate newMissionTemplate = candidates.get(randomIndex);

    // 6. 교체 처리
    oldMission.setMissionTemplate(newMissionTemplate);
    oldMission.setCompleted(false);

    System.out.println("[refreshSingleMission] 랜덤으로 선택된 미션 ID: " + newMissionTemplate.getId());
}




    //미션 완료 처리
    @Transactional
    public void completeMission(Long teamId, Long subGroupId, Long missionTemplateId) {
        // 우선 서브그룹 존재 확인
        SubGroup subGroup = subGroupRepository.findById(subGroupId)
                .orElseThrow(() -> new IllegalArgumentException("서브그룹을 찾을 수 없습니다."));

        // subGroup이 teamId에 속하는지 확인 (필요시, SubGroup 엔티티에 teamId 필드가 있다고 가정)
        if (!subGroup.getTeam().getTeamId().equals(teamId)) {
            throw new IllegalArgumentException("서브그룹이 해당 팀에 속하지 않습니다.");
        }

        // 해당 서브그룹 미션 찾기 (missionTemplateId 기준)
        SubGroupMission mission = subGroupMissionRepository.findBySubGroupAndMissionTemplateId(subGroup, missionTemplateId)
                .orElseThrow(() -> new IllegalArgumentException("해당 미션이 서브그룹에 존재하지 않습니다."));

        // 완료 처리
        mission.setCompleted(true);
    }



}
