package com.ccapp.ccgo.matching.service;

import com.ccapp.ccgo.matching.domain.MbtiScoreProvider;
import com.ccapp.ccgo.matching.domain.PairMatch;
import com.ccapp.ccgo.matching.domain.entity.Answer;
import com.ccapp.ccgo.matching.domain.entity.Question;
import com.ccapp.ccgo.matching.domain.entity.SubGroup;
import com.ccapp.ccgo.matching.domain.entity.SubGroupMember;
import com.ccapp.ccgo.matching.dto.MatchingResponseDto;
import com.ccapp.ccgo.matching.dto.MatchingResultDto;
import com.ccapp.ccgo.matching.repository.SubGroupMemberRepository;
import com.ccapp.ccgo.matching.repository.SubGroupRepository;
import com.ccapp.ccgo.question.repository.AnswerRepository;
import com.ccapp.ccgo.question.repository.QuestionRepository;
import com.ccapp.ccgo.question.dto.AnswerRequestDto;
import com.ccapp.ccgo.question.dto.QuestionRequestDto;
import com.ccapp.ccgo.question.dto.QuestionResponseDto;
import com.ccapp.ccgo.question.dto.QuestionUpdateDto;
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
        if (members.isEmpty()) {
            return new MatchingResponseDto(teamId, "", true, Collections.emptyList());
        }
        Team team = members.get(0).getTeam();

        List<TeamMember> males = members.stream()
                .filter(m -> "MALE".equalsIgnoreCase(m.getUser().getGender()))
                .collect(Collectors.toList());
        List<TeamMember> females = members.stream()
                .filter(m -> "FEMALE".equalsIgnoreCase(m.getUser().getGender()))
                .collect(Collectors.toList());

        List<PairMatch> matchCandidates = createPairMatchCandidates(males, females, teamId);
        matchCandidates.sort(Comparator.comparingDouble(PairMatch::getTotalScore).reversed());

        Set<Long> usedMaleIds = new HashSet<>();
        Set<Long> usedFemaleIds = new HashSet<>();
        List<SubGroup> groups = new ArrayList<>();
        int groupIndex = 1;

        // 1) 커플 우선 매칭
        for (PairMatch pair : matchCandidates) {
            Long maleId = pair.getMale().getUser().getId();
            Long femaleId = pair.getFemale().getUser().getId();
            if (usedMaleIds.contains(maleId) || usedFemaleIds.contains(femaleId)) continue;

            String groupName = team.getTeamName() + groupIndex++;
            SubGroup group = SubGroup.builder()
                    .team(team)
                    .name(groupName)
                    .memberCount(2)
                    .build();
            subGroupRepository.save(group);

            saveSubGroupMember(group, pair.getMale().getUser());
            saveSubGroupMember(group, pair.getFemale().getUser());

            usedMaleIds.add(maleId);
            usedFemaleIds.add(femaleId);
            groups.add(group);
        }

        // 커플로 먼저 만들어진 그룹들만을 "삽입 타깃"으로 고정
        List<SubGroup> coupleGroups = new ArrayList<>(groups);

        List<TeamMember> leftoverMales = males.stream()
                .filter(m -> !usedMaleIds.contains(m.getUser().getId()))
                .collect(Collectors.toList());
        List<TeamMember> leftoverFemales = females.stream()
                .filter(f -> !usedFemaleIds.contains(f.getUser().getId()))
                .collect(Collectors.toList());

        // 2) 잉여 인원 처리
        handleLeftovers(leftoverMales, leftoverFemales, groups, coupleGroups, team, groupIndex, teamId);

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
            int sa = answerRepository.findByUser_IdAndQuestionId(a.getUser().getId(), q.getId())
                    .map(Answer::getScore).orElse(0);
            int sb = answerRepository.findByUser_IdAndQuestionId(b.getUser().getId(), q.getId())
                    .map(Answer::getScore).orElse(0);
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

    // ★ 시그니처 변경: insertTargets = 커플 그룹들
    private void handleLeftovers(
            List<TeamMember> males,
            List<TeamMember> females,
            List<SubGroup> allGroups,
            List<SubGroup> insertTargets,  // 커플로 성사된 그룹만
            Team team,
            int groupIndex,
            Long teamId
    ) {
        // 여자가 없고 남자만 남은 경우
        if (females.isEmpty() && !males.isEmpty()) {
            handleMaleOnlyGroups(males, allGroups, insertTargets, team, groupIndex, teamId);
            return;
        }

        // 남자가 없고 여자만 남은 경우
        if (males.isEmpty() && !females.isEmpty()) {
            handleFemaleOnlyGroups(females, allGroups, insertTargets, team, groupIndex, teamId);
            return;
        }

        // 남자 2명 남으면 남남 그룹 생성 (규칙)
        if (males.size() == 2) {
            SubGroup g = SubGroup.builder()
                    .team(team)
                    .name(team.getTeamName() + groupIndex++)
                    .memberCount(2)
                    .build();
            subGroupRepository.save(g);
            saveSubGroupMember(g, males.get(0).getUser());
            saveSubGroupMember(g, males.get(1).getUser());
            allGroups.add(g);
            males = Collections.emptyList(); // 소비
        } else if (males.size() == 1) {
            // 남자 1명 남으면 커플 그룹에 삽입
            SubGroup best = findBestGroupToInsert(males.get(0), insertTargets, teamId);
            if (best != null && best.getMemberCount() < MAX_GROUP_SIZE) {
                saveSubGroupMember(best, males.get(0).getUser());
                best.setMemberCount(best.getMemberCount() + 1);
                subGroupRepository.save(best);
            }
            males = Collections.emptyList();
        } else if (males.size() > 2) {
            // 2명씩 남남 그룹으로 소진, 홀수 1명 남으면 커플 그룹에 삽입
            int i = 0;
            while (i + 1 < males.size()) {
                SubGroup g = SubGroup.builder()
                        .team(team)
                        .name(team.getTeamName() + groupIndex++)
                        .memberCount(2)
                        .build();
                subGroupRepository.save(g);
                saveSubGroupMember(g, males.get(i).getUser());
                saveSubGroupMember(g, males.get(i + 1).getUser());
                allGroups.add(g);
                i += 2;
            }
            if (i < males.size()) { // 홀수 1명
                TeamMember last = males.get(i);
                SubGroup best = findBestGroupToInsert(last, insertTargets, teamId);
                if (best != null && best.getMemberCount() < MAX_GROUP_SIZE) {
                    saveSubGroupMember(best, last.getUser());
                    best.setMemberCount(best.getMemberCount() + 1);
                    subGroupRepository.save(best);
                }
            }
            males = Collections.emptyList();
        }

        // 여자 잉여: 새 그룹 만들지 말고 커플 그룹에만 삽입
        for (TeamMember female : females) {
            SubGroup best = findBestGroupToInsert(female, insertTargets, teamId);
            if (best != null && best.getMemberCount() < MAX_GROUP_SIZE && isFemaleInsertable(best, female, females.size())) {
                saveSubGroupMember(best, female.getUser());
                best.setMemberCount(best.getMemberCount() + 1);
                subGroupRepository.save(best);
            }
        }
    }

    // ★ 수정: 남자만 있는 경우도 규칙(남남 2인 그룹) 우선, 홀수는 커플 그룹에 삽입
    private void handleMaleOnlyGroups(
            List<TeamMember> males,
            List<SubGroup> allGroups,
            List<SubGroup> insertTargets, // 커플 그룹
            Team team,
            int groupIndex,
            Long teamId
    ) {
        int maleCount = males.size();
        if (maleCount == 1) {
            SubGroup best = findBestGroupToInsert(males.get(0), insertTargets, teamId);
            if (best != null && best.getMemberCount() < MAX_GROUP_SIZE) {
                saveSubGroupMember(best, males.get(0).getUser());
                best.setMemberCount(best.getMemberCount() + 1);
                subGroupRepository.save(best);
            }
            return;
        }
        // 2명씩 남남 그룹 생성
        int i = 0;
        while (i + 1 < maleCount) {
            SubGroup group = SubGroup.builder()
                    .team(team)
                    .name(team.getTeamName() + groupIndex++)
                    .memberCount(2)
                    .build();
            subGroupRepository.save(group);
            saveSubGroupMember(group, males.get(i).getUser());
            saveSubGroupMember(group, males.get(i + 1).getUser());
            allGroups.add(group);
            i += 2;
        }
        // 홀수 1명 남으면 커플 그룹에 삽입
        if (i < maleCount) {
            TeamMember last = males.get(i);
            SubGroup best = findBestGroupToInsert(last, insertTargets, teamId);
            if (best != null && best.getMemberCount() < MAX_GROUP_SIZE) {
                saveSubGroupMember(best, last.getUser());
                best.setMemberCount(best.getMemberCount() + 1);
                subGroupRepository.save(best);
            }
        }
    }

    // ★ 완전 변경: 여자만 있는 경우 기본은 커플 그룹 삽입만 허용
    // 단, 커플 그룹이 하나도 없다면(완전 무매칭) 예전 분할 방식 폴백 적용
    private void handleFemaleOnlyGroups(
            List<TeamMember> females,
            List<SubGroup> allGroups,
            List<SubGroup> insertTargets,
            Team team,
            int groupIndex,
            Long teamId
    ) {
        if (insertTargets == null || insertTargets.isEmpty()) {
            // 폴백: 커플 그룹이 없으면 이전 방식으로 안전 분할
            fallbackSplitFemaleOnly(females, allGroups, team, groupIndex, teamId);
            return;
        }

        for (TeamMember female : females) {
            SubGroup best = findBestGroupToInsert(female, insertTargets, teamId);
            if (best != null && best.getMemberCount() < MAX_GROUP_SIZE && isFemaleInsertable(best, female, females.size())) {
                saveSubGroupMember(best, female.getUser());
                best.setMemberCount(best.getMemberCount() + 1);
                subGroupRepository.save(best);
            }
        }
    }

    // 폴백 로직: 커플 그룹이 0개인 경우에만 사용 (이전 구현 유지)
    private void fallbackSplitFemaleOnly(
            List<TeamMember> females,
            List<SubGroup> groups,
            Team team,
            int groupIndex,
            Long teamId
    ) {
        int femaleCount = females.size();

        if (femaleCount <= 3) {
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
            createFemaleGroup(females.subList(0, 2), groups, team, groupIndex++, teamId);
            createFemaleGroup(females.subList(2, 4), groups, team, groupIndex++, teamId);
        } else if (femaleCount == 5) {
            createFemaleGroup(females.subList(0, 2), groups, team, groupIndex++, teamId);
            createFemaleGroup(females.subList(2, 5), groups, team, groupIndex++, teamId);
        } else if (femaleCount == 6) {
            createFemaleGroup(females.subList(0, 2), groups, team, groupIndex++, teamId);
            createFemaleGroup(females.subList(2, 4), groups, team, groupIndex++, teamId);
            createFemaleGroup(females.subList(4, 6), groups, team, groupIndex++, teamId);
        } else {
            int groupCount = femaleCount / 2;
            for (int i = 0; i < groupCount; i++) {
                int startIndex = i * 2;
                int endIndex = Math.min(startIndex + 2, femaleCount);
                createFemaleGroup(females.subList(startIndex, endIndex), groups, team, groupIndex++, teamId);
            }
            if (femaleCount % 2 == 1) {
                TeamMember remainingFemale = females.get(femaleCount - 1);
                SubGroup bestGroup = findBestGroupToInsert(remainingFemale, groups, teamId);
                if (bestGroup != null && bestGroup.getMemberCount() < MAX_GROUP_SIZE) {
                    saveSubGroupMember(bestGroup, remainingFemale.getUser());
                    bestGroup.setMemberCount(bestGroup.getMemberCount() + 1);
                    subGroupRepository.save(bestGroup);
                }
            }
        }
    }

    // 여자 그룹 생성 헬퍼 (폴백에서만 사용)
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
            // 남자 2+ / 여자 0 구성에, 여자가 잔여가 1명 초과면 먼저 다른 커플부터 채우도록 억제
            if (maleNum > 1 && femaleNum == 0 && totalFemaleLeft > 1) return false;
        }
        return true;
    }

    // ★ 시그니처 변경: 삽입 후보 그룹을 파라미터로 (커플 그룹만 넘기도록)
    private SubGroup findBestGroupToInsert(TeamMember tm, List<SubGroup> candidateGroups, Long teamId) {
        if (candidateGroups == null || candidateGroups.isEmpty()) return null;

        // 1) 2명 그룹(커플) 우선
        Optional<SubGroup> twoMemberGroup = candidateGroups.stream()
                .filter(g -> g.getMemberCount() == 2)
                .filter(g -> g.getMemberCount() < MAX_GROUP_SIZE)
                .max(Comparator.comparingDouble(g -> calculateAverageSimilarity(tm, g, teamId)));

        if (twoMemberGroup.isPresent()) return twoMemberGroup.get();

        // 2) 3명 그룹 중 최대 유사도
        Optional<SubGroup> threeMemberGroup = candidateGroups.stream()
                .filter(g -> g.getMemberCount() == 3)
                .filter(g -> g.getMemberCount() < MAX_GROUP_SIZE)
                .max(Comparator.comparingDouble(g -> calculateAverageSimilarity(tm, g, teamId)));

        if (threeMemberGroup.isPresent()) return threeMemberGroup.get();

        // 3) 여유 있는 그룹 중 최대 유사도
        return candidateGroups.stream()
                .filter(g -> g.getMemberCount() < MAX_GROUP_SIZE)
                .max(Comparator.comparingDouble(g -> calculateAverageSimilarity(tm, g, teamId)))
                .orElse(null);
    }

    private double calculateAverageSimilarity(TeamMember user, SubGroup group, Long teamId) {
        List<SubGroupMember> members = subGroupMemberRepository.findBySubGroup_Id(group.getId());
        double total = 0;
        for (SubGroupMember m : members) {
            TeamMember tm = teamMemberRepository.findByUser_IdAndTeam_TeamId(m.getUser().getId(), teamId)
                    .orElseThrow();
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
                TeamMember tm = teamMemberRepository.findByUser_IdAndTeam_TeamId(u.getId(), team.getTeamId())
                        .orElseThrow();
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

    // ====== 설문/질문 CRUD ======

    @Transactional
    public void saveAnswers(AnswerRequestDto dto, User user) {
        Long userId = user.getId();
        Long teamId = dto.getTeamId();

        List<Question> teamQuestions = questionRepository.findByTeam_TeamId(teamId);
        List<Long> teamQuestionIds = teamQuestions.stream()
                .map(Question::getId)
                .collect(Collectors.toList());
        List<Answer> existingAnswers = answerRepository.findByUser_Id(userId).stream()
                .filter(ans -> teamQuestionIds.contains(ans.getQuestionId()))
                .collect(Collectors.toList());
        answerRepository.deleteAll(existingAnswers);

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));
        List<Answer> newAnswers = dto.getAnswers().stream()
                .map(single -> Answer.builder()
                        .user(user)
                        .questionId(single.getQuestionId())
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

    @Transactional
    public void updateQuestion(Long questionId, QuestionUpdateDto dto) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found: " + questionId));

        question.setText(dto.getText());
    }

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

    @Transactional
    public void deleteQuestion(Long questionId) {
        List<Answer> answers = answerRepository.findByQuestionId(questionId);
        answerRepository.deleteAll(answers);
        questionRepository.deleteById(questionId);
    }

    /**
     * 매칭된 팀원 이름 조회
     */
    @Transactional(readOnly = true)
    public List<String> getMatchedUserNames(Long userId, Long teamId) {
        log.info("[Matching] 매칭된 팀원 조회 | userId: {}, teamId: {}", userId, teamId);

        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자가 존재하지 않습니다."));

        boolean isMember = teamMemberRepository.existsByUser_IdAndTeam_TeamIdAndIsActiveTrue(userId, teamId);
        if (!isMember) {
            throw new RuntimeException("해당 팀에 소속되어 있지 않습니다.");
        }

        List<User> matchedUsers = subGroupMemberRepository.findTeamMatchedMembersExcludingUser(userId, teamId);

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
