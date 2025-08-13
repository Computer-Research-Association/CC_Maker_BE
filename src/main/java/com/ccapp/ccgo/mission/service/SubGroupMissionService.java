package com.ccapp.ccgo.mission.service;

import com.ccapp.ccgo.matching.domain.entity.SubGroup;
import com.ccapp.ccgo.matching.domain.entity.SubGroupMember;
import com.ccapp.ccgo.matching.repository.SubGroupRepository;
import com.ccapp.ccgo.matching.repository.SubGroupMemberRepository;
import com.ccapp.ccgo.mission.dto.ScoreboardResponseDto;
import com.ccapp.ccgo.mission.dto.SubGroupMissionDto;
import com.ccapp.ccgo.mission.dto.SubGroupScoreDto;
import com.ccapp.ccgo.mission.dto.MissionHistoryDto;
import com.ccapp.ccgo.mission.entity.MissionTemplate;
import com.ccapp.ccgo.mission.entity.SubGroupMission;
import com.ccapp.ccgo.mission.entity.MissionHistory;
import com.ccapp.ccgo.mission.repository.MissionTemplateRepository;
import com.ccapp.ccgo.mission.repository.SubGroupMissionRepository;
import com.ccapp.ccgo.mission.repository.MissionHistoryRepository;
import lombok.RequiredArgsConstructor;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SubGroupMissionService {

    private final SubGroupRepository subGroupRepository;
    private final SubGroupMemberRepository subGroupMemberRepository;
    private final MissionTemplateRepository missionTemplateRepository;
    private final SubGroupMissionRepository subGroupMissionRepository;
    private final MissionHistoryRepository missionHistoryRepository;

    // 서브그룹에 미션 부여
    @Transactional
    public void assignMissionsToSubGroup(Long subGroupId) {
        SubGroup subGroup = subGroupRepository.findById(subGroupId)
                .orElseThrow(() -> new IllegalArgumentException("서브그룹을 찾을 수 없습니다."));

        if (!subGroupMissionRepository.findBySubGroup(subGroup).isEmpty()) {
            throw new IllegalStateException("이미 이 서브그룹에 미션이 부여되어 있습니다.");
        }

        // 그룹장 중복 미션 방지: 이미 미션을 받은 사용자가 있는지 체크
        List<SubGroupMember> groupMembers = subGroupMemberRepository.findBySubGroup_Id(subGroupId);
        List<Long> groupMemberIds = groupMembers.stream()
                .map(member -> member.getUser().getId())
                .collect(Collectors.toList());

        for (Long memberId : groupMemberIds) {
            boolean hasExistingMission = subGroupMissionRepository.existsByTeamIdAndUserId(
                    subGroup.getTeam().getTeamId(), memberId);
            if (hasExistingMission) {
                throw new IllegalStateException("사용자 ID " + memberId + "가 이미 다른 서브그룹에서 미션을 받았습니다.");
            }
        }
        assignMissionsByScore(subGroup, 1, 6);
        assignMissionsByScore(subGroup, 3, 6);
        assignMissionsByScore(subGroup, 5, 6);
        assignMissionsByScore(subGroup, 10, 6);
    }

    private void assignMissionsByScore(SubGroup subGroup, Integer score, int count) {
        List<MissionTemplate> missions = missionTemplateRepository.findByScore(score);
        if (missions.size() < count) {
            throw new IllegalStateException(score + "점 미션이 최소 " + count + "개 이상 필요합니다.");
        }

        Collections.shuffle(missions);
        List<MissionTemplate> selected = missions.subList(0, count);

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
        
        // 미션 히스토리에 저장
        saveMissionHistory(subGroup, mission.getMissionTemplate());
    }
    
    // 미션 히스토리 저장
    private void saveMissionHistory(SubGroup subGroup, MissionTemplate missionTemplate) {
        // 서브그룹의 모든 멤버에 대해 히스토리 저장
        List<SubGroupMember> members = subGroupMemberRepository.findBySubGroup_Id(subGroup.getId());
        
        for (SubGroupMember member : members) {
            MissionHistory history = MissionHistory.builder()
                    .subGroup(subGroup)
                    .team(subGroup.getTeam())
                    .user(member.getUser())
                    .missionTemplate(missionTemplate)
                    .completedAt(LocalDateTime.now())
                    .build();
            
                    missionHistoryRepository.save(history);
        }
    }
    
    // 서브그룹의 미션 히스토리 조회
    @Transactional(readOnly = true)
    public List<MissionHistoryDto> getMissionHistoryBySubGroup(Long subGroupId) {
        List<MissionHistory> histories = missionHistoryRepository.findBySubGroup_IdOrderByCompletedAtDesc(subGroupId);
        
        return histories.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    // 사용자의 미션 히스토리 조회 (팀별)
    @Transactional(readOnly = true)
    public List<MissionHistoryDto> getMissionHistoryByUser(Long userId, Long teamId) {
        try {
            List<MissionHistory> histories = missionHistoryRepository.findByUser_IdAndTeam_TeamIdOrderByCompletedAtDesc(userId, teamId);
            
            return histories.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // 테이블이 존재하지 않는 경우 등 오류 발생 시 빈 리스트 반환
            System.err.println("미션 히스토리 조회 중 오류 발생: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    // 팀의 미션 히스토리 조회
    @Transactional(readOnly = true)
    public List<MissionHistoryDto> getMissionHistoryByTeam(Long teamId) {
        List<MissionHistory> histories = missionHistoryRepository.findByTeamIdOrderByCompletedAtDesc(teamId);
        
        return histories.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    // MissionHistory를 DTO로 변환
    private MissionHistoryDto convertToDto(MissionHistory history) {
        // 미션을 완료한 사용자와 매칭된 상대방들의 이름 조회
        List<String> matchedNames = getMatchedUserNames(history.getUser().getId(), history.getTeam().getTeamId());
        
        return MissionHistoryDto.builder()
                .id(history.getId())
                .subGroupId(history.getSubGroup().getId())
                .subGroupName(history.getSubGroup().getName())
                .teamId(history.getTeam().getTeamId())
                .teamName(history.getTeam().getTeamName())
                .userId(history.getUser().getId())
                .userName(history.getUser().getName())
                .matchedNames(matchedNames) // 매칭된 상대방들의 이름 추가
                .missionTemplateId(history.getMissionTemplate().getId())
                .missionTitle(history.getMissionTemplate().getTitle())
                .missionDescription(history.getMissionTemplate().getDescription())
                .missionScore(history.getMissionTemplate().getScore())
                .completedAt(history.getCompletedAt())
                .createdAt(history.getCreatedAt())
                .build();
    }
    
    // 사용자와 매칭된 상대방들의 이름 조회
    private List<String> getMatchedUserNames(Long userId, Long teamId) {
        try {
            System.out.println("[getMatchedUserNames] userId: " + userId + ", teamId: " + teamId);
            
            // 사용자가 속한 서브그룹 조회
            Optional<Long> subGroupIdOpt = subGroupMemberRepository.findSubGroupIdByTeamIdAndUserId(teamId, userId);
            if (subGroupIdOpt.isEmpty()) {
                System.out.println("[getMatchedUserNames] 서브그룹을 찾을 수 없음");
                return new ArrayList<>();
            }
            
            Long subGroupId = subGroupIdOpt.get();
            System.out.println("[getMatchedUserNames] subGroupId: " + subGroupId);
            
            // 같은 서브그룹의 다른 멤버들 조회 (본인 제외)
            List<SubGroupMember> members = subGroupMemberRepository.findBySubGroup_Id(subGroupId);
            System.out.println("[getMatchedUserNames] 전체 멤버 수: " + members.size());
            
            List<String> matchedNames = members.stream()
                    .map(member -> member.getUser().getName())
                    .filter(name -> !name.equals(members.stream()
                            .filter(m -> m.getUser().getId().equals(userId))
                            .findFirst()
                            .map(m -> m.getUser().getName())
                            .orElse("")))
                    .collect(Collectors.toList());
            
            System.out.println("[getMatchedUserNames] 매칭된 이름들: " + matchedNames);
            return matchedNames;
        } catch (Exception e) {
            System.err.println("[getMatchedUserNames] 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
