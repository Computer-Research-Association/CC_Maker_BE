package com.ccapp.ccgo.matching.service;

import com.ccapp.ccgo.matching.domain.MbtiScoreProvider;
import com.ccapp.ccgo.matching.domain.PairMatch;
import com.ccapp.ccgo.matching.domain.entity.*;
import com.ccapp.ccgo.matching.dto.MatchingResponseDto;
import com.ccapp.ccgo.matching.dto.MatchingResultDto;
import com.ccapp.ccgo.matching.repository.*;
import com.ccapp.ccgo.question.dto.AnswerRequestDto;
import com.ccapp.ccgo.question.dto.QuestionRequestDto;
import com.ccapp.ccgo.question.dto.QuestionResponseDto;
import com.ccapp.ccgo.question.dto.QuestionUpdateDto;
import com.ccapp.ccgo.question.repository.AnswerRepository;
import com.ccapp.ccgo.question.repository.QuestionRepository;
import com.ccapp.ccgo.team.entity.Team;
import com.ccapp.ccgo.team.entity.TeamMember;
import com.ccapp.ccgo.team.repository.TeamMemberRepository;
import com.ccapp.ccgo.team.repository.TeamRepository;
import com.ccapp.ccgo.user.dto.UserResponseDto;
import com.ccapp.ccgo.user.entity.User;
import com.ccapp.ccgo.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingService {

    private final TeamMemberRepository teamMemberRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final SubGroupRepository subGroupRepository;
    private final SubGroupMemberRepository subGroupMemberRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final MbtiScoreProvider mbtiScoreProvider;

    private static final int MAX_GROUP_SIZE = 4;
    private static final double MBTI_WEIGHT = 0.5;
    private static final double SIMILARITY_WEIGHT = 0.5;

    @Transactional
    public MatchingResponseDto performMatching(Long teamId) {
        List<TeamMember> members = teamMemberRepository.findByTeam_TeamIdAndIsActiveTrue(teamId);
        Team team = members.get(0).getTeam();

        List<TeamMember> males = members.stream().filter(m -> "MALE".equalsIgnoreCase(m.getUser().getGender())).collect(Collectors.toList());
        List<TeamMember> females = members.stream().filter(m -> "FEMALE".equalsIgnoreCase(m.getUser().getGender())).collect(Collectors.toList());

        List<PairMatch> matchCandidates = createPairMatchCandidates(males, females, teamId);
        matchCandidates.sort(Comparator.comparingDouble(PairMatch::getTotalScore).reversed());

        Set<Long> usedMaleIds = new HashSet<>();
        Set<Long> usedFemaleIds = new HashSet<>();
        List<SubGroup> groups = new ArrayList<>();
        int groupIndex = 1;

        for (PairMatch pair : matchCandidates) {
            Long maleId = pair.getMale().getUser().getId();
            Long femaleId = pair.getFemale().getUser().getId();
            if (usedMaleIds.contains(maleId) || usedFemaleIds.contains(femaleId)) continue;

            String groupName = team.getTeamName() + groupIndex++;
            SubGroup group = SubGroup.builder().team(team).name(groupName).memberCount(2).build();
            subGroupRepository.save(group);

            saveSubGroupMember(group, pair.getMale().getUser());
            saveSubGroupMember(group, pair.getFemale().getUser());

            usedMaleIds.add(maleId);
            usedFemaleIds.add(femaleId);
            groups.add(group);
        }

        List<TeamMember> leftoverMales = males.stream().filter(m -> !usedMaleIds.contains(m.getUser().getId())).collect(Collectors.toList());
        List<TeamMember> leftoverFemales = females.stream().filter(f -> !usedFemaleIds.contains(f.getUser().getId())).collect(Collectors.toList());

        handleLeftovers(leftoverMales, leftoverFemales, groups, team, groupIndex, teamId);

        return buildMatchingResponseDto(team, groups);
    }

    private List<PairMatch> createPairMatchCandidates(List<TeamMember> males, List<TeamMember> females, Long teamId) {
        List<PairMatch> result = new ArrayList<>();
        for (TeamMember m : males) {
            for (TeamMember f : females) {
                double mbti = calculateMbtiScore(m, f);
                double sim = calculateSimilarity(m, f, teamId);
                result.add(new PairMatch(m, f, mbti * MBTI_WEIGHT + sim * SIMILARITY_WEIGHT));
            }
        }
        return result;
    }

    private double calculateSimilarity(TeamMember a, TeamMember b, Long teamId) {
        List<Question> questions = questionRepository.findByTeam_TeamId(teamId);
        int totalSim = 0;

        for (Question q : questions) {
            int sa = answerRepository.findByUser_IdAndQuestionId(a.getUser().getId(), q.getId()).map(Answer::getScore).orElse(0);
            int sb = answerRepository.findByUser_IdAndQuestionId(b.getUser().getId(), q.getId()).map(Answer::getScore).orElse(0);
            totalSim += Math.max(0, 5 - Math.abs(sa - sb));
        }

        return questions.isEmpty()
                ? 0
                : (double) totalSim / (5 * questions.size()) * 100;
    }

    private double calculateMbtiScore(TeamMember a, TeamMember b) {
        String mbtiA = a.getMbti();
        String mbtiB = b.getMbti();
        if (mbtiA == null || mbtiB == null) return 0;
        return mbtiScoreProvider.getScore(mbtiA, mbtiB) + mbtiScoreProvider.getScore(mbtiB, mbtiA);
    }

    private void handleLeftovers(List<TeamMember> males, List<TeamMember> females, List<SubGroup> groups, Team team, int groupIndex, Long teamId) {
        // 남자만 있는 경우 처리
        if (females.isEmpty() && !males.isEmpty()) {
            handleMaleOnlyGroups(males, groups, team, groupIndex, teamId);
            return;
        }
        
        // 여자만 있는 경우 처리
        if (males.isEmpty() && !females.isEmpty()) {
            handleFemaleOnlyGroups(females, groups, team, groupIndex, teamId);
            return;
        }
        
        if (males.size() == 2) {
            SubGroup g = SubGroup.builder().team(team).name(team.getTeamName() + groupIndex++).memberCount(2).build();
            subGroupRepository.save(g);
            saveSubGroupMember(g, males.get(0).getUser());
            saveSubGroupMember(g, males.get(1).getUser());
            groups.add(g);
        } else if (males.size() == 1) {
            // 1명이 남으면 유사도 기준으로 기존 그룹에 추가 (1명짜리 그룹 절대 생성 금지)
            SubGroup best = findBestGroupToInsert(males.get(0), groups, teamId);
            if (best != null) {
                saveSubGroupMember(best, males.get(0).getUser());
                best.setMemberCount(best.getMemberCount() + 1);
                subGroupRepository.save(best);
            }
        }

        for (TeamMember female : females) {
            // 여자도 유사도 기준으로 기존 그룹에 추가 (1명짜리 그룹 절대 생성 금지)
            SubGroup best = findBestGroupToInsert(female, groups, teamId);
            if (best != null && isFemaleInsertable(best, female, females.size())) {
                saveSubGroupMember(best, female.getUser());
                best.setMemberCount(best.getMemberCount() + 1);
                subGroupRepository.save(best);
            }
        }
    }
    
    // 남자만 있는 경우 그룹 생성 (2명씩 커플로)
    private void handleMaleOnlyGroups(List<TeamMember> males, List<SubGroup> groups, Team team, int groupIndex, Long teamId) {
        int maleCount = males.size();
        
        // 2명씩 커플로 만들기
        for (int i = 0; i < maleCount; i += 2) {
            int endIndex = Math.min(i + 2, maleCount);
            List<TeamMember> groupMembers = males.subList(i, endIndex);
            
            if (groupMembers.size() >= 2) {
                SubGroup group = SubGroup.builder()
                        .team(team)
                        .name(team.getTeamName() + groupIndex++)
                        .memberCount(groupMembers.size())
                        .build();
                subGroupRepository.save(group);
                
                for (TeamMember member : groupMembers) {
                    saveSubGroupMember(group, member.getUser());
                }
                groups.add(group);
            } else if (groupMembers.size() == 1) {
                // 1명이 남으면 유사도 기준으로 기존 그룹에 추가 (1명짜리 그룹 절대 생성 금지)
                SubGroup best = findBestGroupToInsert(groupMembers.get(0), groups, teamId);
                if (best != null) {
                    saveSubGroupMember(best, groupMembers.get(0).getUser());
                    best.setMemberCount(best.getMemberCount() + 1);
                    subGroupRepository.save(best);
                }
            }
        }
    }
    
    // 여자만 있는 경우 그룹 생성
    private void handleFemaleOnlyGroups(List<TeamMember> females, List<SubGroup> groups, Team team, int groupIndex, Long teamId) {
        int femaleCount = females.size();
        
        if (femaleCount <= 3) {
            // 3명 이하면 하나의 그룹으로
            SubGroup group = SubGroup.builder()
                    .team(team)
                    .name(team.getTeamName() + groupIndex++)
                    .memberCount(femaleCount)
                    .build();
            subGroupRepository.save(group);
            
            for (TeamMember member : females) {
                saveSubGroupMember(group, member.getUser());
            }
            groups.add(group);
        } else if (femaleCount == 4) {
            // 4명일 때: 2/2
            createFemaleGroup(females.subList(0, 2), groups, team, groupIndex++, teamId);
            createFemaleGroup(females.subList(2, 4), groups, team, groupIndex++, teamId);
        } else if (femaleCount == 5) {
            // 5명일 때: 2/3
            createFemaleGroup(females.subList(0, 2), groups, team, groupIndex++, teamId);
            createFemaleGroup(females.subList(2, 5), groups, team, groupIndex++, teamId);
        } else if (femaleCount == 6) {
            // 6명일 때: 2/2/2
            createFemaleGroup(females.subList(0, 2), groups, team, groupIndex++, teamId);
            createFemaleGroup(females.subList(2, 4), groups, team, groupIndex++, teamId);
            createFemaleGroup(females.subList(4, 6), groups, team, groupIndex++, teamId);
        } else {
            // 6명 초과일 때: 2명씩 그룹으로 나누고, 남은 사람들은 기존 그룹에 추가
            int groupCount = femaleCount / 2;
            for (int i = 0; i < groupCount; i++) {
                int startIndex = i * 2;
                int endIndex = Math.min(startIndex + 2, femaleCount);
                createFemaleGroup(females.subList(startIndex, endIndex), groups, team, groupIndex++, teamId);
            }
            
            // 남은 사람들 처리 (1명이 남은 경우)
            if (femaleCount % 2 == 1) {
                TeamMember remainingFemale = females.get(femaleCount - 1);
                // 설문 조사 결과가 가장 일치하는 그룹에 추가
                SubGroup bestGroup = findBestGroupToInsert(remainingFemale, groups, teamId);
                if (bestGroup != null) {
                    saveSubGroupMember(bestGroup, remainingFemale.getUser());
                    bestGroup.setMemberCount(bestGroup.getMemberCount() + 1);
                    subGroupRepository.save(bestGroup);
                }
            }
        }
    }
    
    // 여자 그룹 생성 헬퍼 메서드
    private void createFemaleGroup(List<TeamMember> groupMembers, List<SubGroup> groups, Team team, int groupIndex, Long teamId) {
        SubGroup group = SubGroup.builder()
                .team(team)
                .name(team.getTeamName() + groupIndex)
                .memberCount(groupMembers.size())
                .build();
        subGroupRepository.save(group);
        
        for (TeamMember member : groupMembers) {
            saveSubGroupMember(group, member.getUser());
        }
        groups.add(group);
    }

    private boolean isFemaleInsertable(SubGroup group, TeamMember female, int totalFemaleLeft) {
        List<SubGroupMember> members = subGroupMemberRepository.findBySubGroup_Id(group.getId());
        long femaleNum = members.stream().filter(m -> "FEMALE".equalsIgnoreCase(m.getUser().getGender())).count();
        long maleNum = members.size() - femaleNum;
        if ("FEMALE".equalsIgnoreCase(female.getUser().getGender())) {
            if (maleNum > 1 && femaleNum == 0 && totalFemaleLeft > 1) return false;
        }
        return true;
    }

    private SubGroup findBestGroupToInsert(TeamMember tm, List<SubGroup> groups, Long teamId) {
        // 1. 4명이 아닌 그룹 중에서 3명이 아닌 그룹 (즉, 2명 그룹) 우선 선택
        Optional<SubGroup> twoMemberGroup = groups.stream()
                .filter(g -> g.getMemberCount() == 2)
                .max(Comparator.comparingDouble(g -> calculateAverageSimilarity(tm, g, teamId)));
        
        if (twoMemberGroup.isPresent()) {
            return twoMemberGroup.get();
        }
        
        // 2. 모든 그룹이 3명이라면 3명 그룹 중에서 유사도가 가장 높은 그룹 선택
        Optional<SubGroup> threeMemberGroup = groups.stream()
                .filter(g -> g.getMemberCount() == 3)
                .max(Comparator.comparingDouble(g -> calculateAverageSimilarity(tm, g, teamId)));
        
        if (threeMemberGroup.isPresent()) {
            return threeMemberGroup.get();
        }
        
        // 3. 모든 그룹이 4명이어도 유사도가 가장 높은 그룹 선택 (1명짜리 그룹 절대 생성 금지)
        return groups.stream()
                .max(Comparator.comparingDouble(g -> calculateAverageSimilarity(tm, g, teamId)))
                .orElse(null);
    }

    private double calculateAverageSimilarity(TeamMember user, SubGroup group, Long teamId) {
        List<SubGroupMember> members = subGroupMemberRepository.findBySubGroup_Id(group.getId());
        double total = 0;
        for (SubGroupMember m : members) {
            TeamMember tm = teamMemberRepository.findByUser_IdAndTeam_TeamId(m.getUser().getId(), teamId).orElseThrow();
            total += calculateSimilarity(user, tm, teamId);
        }
        return members.isEmpty() ? 0 : total / members.size();
    }

    private void saveSubGroupMember(SubGroup group, User user) {
        SubGroupMember m = SubGroupMember.builder().subGroup(group).user(user).build();
        subGroupMemberRepository.save(m);
    }

    private MatchingResponseDto buildMatchingResponseDto(Team team, List<SubGroup> groups) {
        List<MatchingResultDto> result = groups.stream().map(g -> {
            List<SubGroupMember> members = subGroupMemberRepository.findBySubGroup_Id(g.getId());
            List<UserResponseDto> users = members.stream().map(m -> {
                User u = m.getUser();
                TeamMember tm = teamMemberRepository.findByUser_IdAndTeam_TeamId(u.getId(), team.getTeamId()).orElseThrow();
                return UserResponseDto.builder()
                        .id(u.getId())
                        .email(u.getEmail())
                        .name(u.getName())
                        .gender(u.getGender())
                        .birthdate(u.getBirthdate())
                        .createdAt(u.getCreatedAt())
                        .mbti(tm.getMbti())
                        .build();
            }).collect(Collectors.toList());
            return new MatchingResultDto(g.getId(), g.getName(), users);
        }).collect(Collectors.toList());

        return new MatchingResponseDto(team.getTeamId(), team.getTeamName(), true, result);
    }


    // 유저 ID 기준으로 기존 Answer 삭제
    // 해당 유저의 기존 Answer를 삭제하고 새 Answer를 저장
    // 새 Answer를 전부 INSERT
    // MatchingService가 매칭 돌릴 때 최신 데이터를 사용
    @Transactional
    public void saveAnswers(AnswerRequestDto dto, User user) {
        Long userId = user.getId();
        Long teamId = dto.getTeamId();

        // 해당 팀의 질문만 필터링해서 삭제
        List<Question> teamQuestions = questionRepository.findByTeam_TeamId(teamId);
        List<Long> teamQuestionIds = teamQuestions.stream()
                .map(Question::getId)
                .collect(Collectors.toList());
        List<Answer> existingAnswers = answerRepository.findByUser_Id(userId).stream()
                .filter(ans -> teamQuestionIds.contains(ans.getQuestionId()))
                .collect(Collectors.toList());
        answerRepository.deleteAll(existingAnswers);

        // 덮어쓰기

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));
        List<Answer> newAnswers = dto.getAnswers().stream()
                .map(single -> Answer.builder()
                        .user(user)
                        .questionId(single.getQuestionId())  // question 객체 대신 questionId 직접 저장
                        .score(single.getScore())
                        .team(team)
                        .build())
                .collect(Collectors.toList());

        answerRepository.saveAll(newAnswers);

        if (dto.getMbti() != null && !dto.getMbti().isEmpty()) {
            TeamMember teamMember = teamMemberRepository
                    .findByUser_IdAndTeam_TeamId(userId, teamId)
                    .orElseThrow(() -> new IllegalArgumentException("TeamMember not found"));

            teamMember.setMbti(dto.getMbti());
            teamMemberRepository.save(teamMember);
        }
    }

    // 한 번에 여러 개의 새로운 Question을 DB에 등록
    @Transactional
    public void createQuestions(QuestionRequestDto dto) {
        Long teamId = dto.getTeamId();

        List<Question> questions = dto.getQuestions().stream()
                .map(q -> Question.builder()
                        .team(Team.builder().teamId(teamId).build())
                        .text(q)
                        .build())
                .collect(Collectors.toList());

        questionRepository.saveAll(questions);
    }


    // 특정 Question의 질문 내용을 수정
    @Transactional
    public void updateQuestion(Long questionId, QuestionUpdateDto dto) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found: " + questionId));

        question.setText(dto.getText());
    }


    // 특정 팀의 모든 질문 리스트를 조회 (읽기 전용 트랜잭션)
    @Transactional(readOnly = true)
    public List<QuestionResponseDto> getQuestions(Long teamId) {
        List<Question> questions = questionRepository.findByTeam_TeamId(teamId);

        return questions.stream()
                .map(q -> QuestionResponseDto.builder()
                        .id(q.getId())
                        .text(q.getText())
                        .build())
                .collect(Collectors.toList());
    }


    // Question에 연결된 Answer들을 먼저 삭제한 뒤, Question을 삭제
    @Transactional
    public void deleteQuestion(Long questionId) {
        // 삭제할 Question과 연결된 모든 Answer 레코드 조회
        List<Answer> answers = answerRepository.findByQuestionId(questionId);
        // 조회된 Answer 레코드 전부 삭제
        answerRepository.deleteAll(answers);
        // 마지막으로 Question 자체 삭제
        questionRepository.deleteById(questionId);
    }

    /**
     * 매칭된 팀원 이름 조회
     */
    @Transactional(readOnly = true)
    public List<String> getMatchedUserNames(Long userId, Long teamId) {
        log.info("[Matching] 매칭된 팀원 조회 | userId: {}, teamId: {}", userId, teamId);

        // 1. 유저 존재 확인
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자가 존재하지 않습니다."));

        // 2. 팀 소속 여부 확인 (활성 멤버만)
        boolean isMember = teamMemberRepository.existsByUser_IdAndTeam_TeamIdAndIsActiveTrue(userId, teamId);
        if (!isMember) {
            throw new RuntimeException("해당 팀에 소속되어 있지 않습니다.");
        }

        // 3. 유저가 속한 SubGroup 멤버(본인 제외) 조회
        List<User> matchedUsers = subGroupMemberRepository.findTeamMatchedMembersExcludingUser(userId, teamId);

        // 4. 이름만 리스트로 변환 후 반환
        List<String> matchedNames = matchedUsers.stream()
                .map(User::getName)
                .collect(Collectors.toList());

        log.info("[Matching] 매칭된 팀원 수: {}", matchedNames.size());
        return matchedNames;
    }



    /**
     * 사용자의 서브그룹 ID 조회
     */
    public Long findSubGroupIdByTeamIdAndUserId(Long teamId, Long userId) {
        return subGroupMemberRepository.findSubGroupIdByTeamIdAndUserId(teamId, userId)
                .orElse(null);
    }


}
