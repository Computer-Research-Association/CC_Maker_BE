package com.ccapp.ccgo.service;

import com.ccapp.ccgo.dto.*;
import com.ccapp.ccgo.matching.*;
import com.ccapp.ccgo.repository.*;
import com.ccapp.ccgo.team.Team;
import com.ccapp.ccgo.team.TeamMember;
import com.ccapp.ccgo.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchingService {

    private final TeamMemberRepository teamMemberRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final SubGroupRepository subGroupRepository;
    private final SubGroupMemberRepository subGroupMemberRepository;
    // 가중치
    private static final double MBTI_WEIGHT = 0.5;
    private static final double SIMILARITY_WEIGHT = 0.5;

    private final MbtiScoreProvider mbtiScoreProvider;
    private final UserRepository userRepository;

    @Transactional
    public MatchingResponseDto performMatching(Long teamId) {

        // 1. 팀 정보 가져오기
        // 해당 팀에 소속된 팀원 전체 가져옴
        // 팀원이 없다면 (비어있다면) IllegalArgumentException
        List<TeamMember> members = teamMemberRepository.findByTeam_TeamIdAndIsActiveTrue(teamId);

        if (members.isEmpty()) {
            throw new IllegalArgumentException("해당 팀(" + teamId + ")에 유저가 없습니다.");
        }

        Map<Long, TeamMember> memberMap = members.stream()
                .collect(Collectors.toMap(tm -> tm.getUser().getId(), tm -> tm));



        Team team = members.get(0).getTeam();

        // 2. 남/여 그룹 나누기

        // 남자 리스트
        List<TeamMember> males = members.stream()
                .filter(m -> "MALE".equalsIgnoreCase(m.getUser().getGender()))
                .collect(Collectors.toList());

        // 여자 리스트
        List<TeamMember> females = members.stream()
                .filter(m -> "FEMALE".equalsIgnoreCase(m.getUser().getGender()))
                .collect(Collectors.toList());

        List<TeamMember> groupA;
        List<TeamMember> groupB;

        // 기준 잡기
        // 수가 적은 쪽이 기준 / 수가 같을 경우 남자가 기준
        if (males.size() < females.size()) {
            groupA = males;
            groupB = females;
        } else if (females.size() < males.size()) {
            groupA = females;
            groupB = males;
        } else {
            groupA = males;
            groupB = females;
        }

        // 3. 후보 리스트 Map 생성
        // Map을 통한 후보 리스트 조합 생성 (groupA 의 각 멤버별로 groupB 와 전부 조합)
        Map<Long, List<PairMatch>> candidateMap = createCandidateMap(groupA, groupB, teamId);

        // 4. 모든 쌍 점수 계산
        // 후보 Map에 담긴 후보들로 A-B 모든 쌍 생성, 다시 MBTI + 유사도 점수 합산
        // PairMatch 객체에 저장
        List<PairMatch> pairMatches = generateAllPairMatches(candidateMap, memberMap);

        // 5. 점수 내림차순 정렬
        pairMatches.sort(Comparator.comparingDouble(PairMatch::getTotalScore).reversed());

        // 6. Greedy 매칭 수행
        // 이미 매칭된 유저가 포함된 쌍은 건너뜀
        // 새로 생성되는 그룹은 SubGroup
        // SubGroupMember 테이블에 두 사람 매핑
        // 매칭 시 그룹명은 팀이름 + index (홍길통A1, 홍길동A2 ...)
        Set<Long> matchedUserIds = new HashSet<>();
        List<SubGroup> subGroups = new ArrayList<>();
        int groupIndex = 1;

        for (PairMatch pair : pairMatches) {
            if (matchedUserIds.contains(pair.userA.getUser().getId())
                    || matchedUserIds.contains(pair.userB.getUser().getId())) {
                continue;
            }

            String groupName = team.getTeamName() + groupIndex;
            SubGroup sg = SubGroup.builder()
                    .team(team)
                    .name(groupName)
                    .build();
            subGroupRepository.save(sg);

            saveSubGroupMember(sg, pair.userA.getUser());
            saveSubGroupMember(sg, pair.userB.getUser());

            matchedUserIds.add(pair.userA.getUser().getId());
            matchedUserIds.add(pair.userB.getUser().getId());

            subGroups.add(sg);
            groupIndex++;
        }

        // 7. 잉여 처리
        handleLeftovers(groupA, groupB, matchedUserIds, team, groupIndex, subGroups);

        // 8. 결과 DTO 변환
        return buildMatchingResponseDto(team, subGroups);
    }

    // groupA의 각 멤버에게 groupB 중 top N 후보자 리스트를 만들어줌
    // 점수 높은 순서대로 5명 (동점자 포함) 까지
    private Map<Long, List<PairMatch>> createCandidateMap(List<TeamMember> groupA,
                                                           List<TeamMember> groupB,
                                                           Long teamId) {
        Map<Long, List<PairMatch>> candidateMap = new HashMap<>();

        for (TeamMember tmA : groupA) {
            List<PairMatch> tempList = new ArrayList<>();

            for (TeamMember tmB : groupB) {
                int mbtiScore = calculateMbtiTotalScore(tmA, tmB);
                double similarityScore = calculateSimilarityScore(tmA, tmB, teamId);
                // 가중치 계산으로 조정
                double totalScore = mbtiScore * MBTI_WEIGHT + similarityScore * SIMILARITY_WEIGHT;

                tempList.add(new PairMatch(tmA, tmB, totalScore));
            }

            tempList.sort(Comparator.comparingDouble(PairMatch::getTotalScore).reversed());

            List<PairMatch> topCandidates = new ArrayList<>();
            double lastScore = -1;

            for (int i = 0; i < tempList.size(); i++) {
                PairMatch pm = tempList.get(i);
                if (i < 5) {
                    topCandidates.add(pm);
                    lastScore = pm.totalScore;
                } else if (pm.totalScore == lastScore) {
                    topCandidates.add(pm);
                } else {
                    break;
                }
            }

            candidateMap.put(tmA.getUser().getId(), topCandidates);
        }

        return candidateMap;
    }

    // candidateMap 기반으로 모든 (A,B) 쌍에 대해 PairMatch 생성
    // 다시 MBTI 점수 + 질문 유사도 점수 계산
    private List<PairMatch> generateAllPairMatches(Map<Long, List<PairMatch>> candidateMap,
                                                   Map<Long, TeamMember> memberMap) {
        List<PairMatch> pairs = new ArrayList<>();

        for (List<PairMatch> pairList : candidateMap.values()) {
            pairs.addAll(pairList);
        }

        return pairs;
    }


    // A→B, B→A MBTI 점수를 각각 구해서 합산
    // 대칭적이지 않을 수도 있다는 점 고려 (현재 데이터는 대칭적임)
    private int calculateMbtiTotalScore(TeamMember a, TeamMember b) {
        String mbtiA = a.getMbti();
        String mbtiB = b.getMbti();
        if (mbtiA == null || mbtiB == null) {
            return 0; // mbti가 없으면 점수 0 처리하거나, 다른 정책 적용
        }
        int scoreAtoB = mbtiScoreProvider.getScore(mbtiA, mbtiB);
        int scoreBtoA = mbtiScoreProvider.getScore(mbtiB, mbtiA);
        return scoreAtoB + scoreBtoA;
    }

    // 팀별로 등록된 질문 리스트 조회
    // 각 질문 별로 A와 B의 점수 차이를 계산 → 유사도 환산
    // 차이가 0이면 유사도 5, 차이가 5면 유사도 0
    // 전체 유사도 점수 → 100% 환산
    private double calculateSimilarityScore(TeamMember a, TeamMember b, Long teamId) {
        Long userIdA = a.getUser().getId();
        Long userIdB = b.getUser().getId();

        List<Answer> answersA = answerRepository.findByUser_Id(userIdA);
        List<Answer> answersB = answerRepository.findByUser_Id(userIdB);

        // 팀별 질문 수 확보
        List<Question> questions = questionRepository.findByTeam_TeamId(teamId);
        int totalQuestions = questions.size();

        int totalSimilarity = 0;

        for (Question q : questions) {
            int scoreA = answersA.stream()
                    .filter(ans -> ans.getQuestion().getId().equals(q.getId()))
                    .map(Answer::getScore)
                    .findFirst()
                    .orElse(0);

            int scoreB = answersB.stream()
                    .filter(ans -> ans.getQuestion().getId().equals(q.getId()))
                    .map(Answer::getScore)
                    .findFirst()
                    .orElse(0);

            int diff = Math.abs(scoreA - scoreB);
            int similarity = 5 - diff;
            totalSimilarity += similarity;
        }

        Double similarityRate = (double) totalSimilarity / (5 * totalQuestions);
        return similarityRate * 100;
    }

    // 6. Greedy 매칭 결과 저장
    private void saveSubGroupMember(SubGroup sg, User user) {
        SubGroupMember sgm = SubGroupMember.builder()
                .subGroup(sg)
                .user(user)
                .build();
        subGroupMemberRepository.save(sgm);
    }

    // 7. 잉여 처리
    private void handleLeftovers(List<TeamMember> groupA,
                                 List<TeamMember> groupB,
                                 Set<Long> matchedUserIds,
                                 Team team,
                                 int groupIndex,
                                 List<SubGroup> subGroups) {

        // 그룹 이름 중복 방지
        Set<String> existingGroupNames = subGroups.stream()
                .map(SubGroup::getName)
                .collect(Collectors.toSet());

        List<TeamMember> leftovers = new ArrayList<>();
        groupA.stream()
                .filter(tm -> !matchedUserIds.contains(tm.getUser().getId()))
                .forEach(leftovers::add);
        groupB.stream()
                .filter(tm -> !matchedUserIds.contains(tm.getUser().getId()))
                .forEach(leftovers::add);

        Iterator<TeamMember> it = leftovers.iterator();

        while (it.hasNext()) {
            TeamMember tm1 = it.next();
            if (it.hasNext()) {
                TeamMember tm2 = it.next();

                // 잉여 그룹 이름 생성
                String groupName;
                do {
                    groupName = team.getTeamName() + groupIndex;
                    groupIndex++;
                } while (existingGroupNames.contains(groupName));
                existingGroupNames.add(groupName);

                SubGroup sg = SubGroup.builder()
                        .team(team)
                        .name(groupName)
                        .build();
                subGroupRepository.save(sg);

                saveSubGroupMember(sg, tm1.getUser());
                saveSubGroupMember(sg, tm2.getUser());

                subGroups.add(sg);

            } else {
                // 홀수 남음 → 기존 그룹 중 가장 점수 높은 그룹으로 편입
                if (!subGroups.isEmpty()) {
                    SubGroup targetGroup = subGroups.stream()
                            .max(Comparator.comparing(sg -> {
                                List<SubGroupMember> members = subGroupMemberRepository.findBySubGroup_Id(sg.getId());
                                return members.size();
                            }))
                            .orElse(subGroups.get(0));
                    saveSubGroupMember(targetGroup, tm1.getUser());
                } else {
                    // 기존 그룹이 없으면 단독 그룹 생성
                    String groupName;
                    do {
                        groupName = team.getTeamName() + groupIndex;
                        groupIndex++;
                    } while (existingGroupNames.contains(groupName));
                    existingGroupNames.add(groupName);

                    SubGroup sg = SubGroup.builder()
                            .team(team)
                            .name(groupName)
                            .build();
                    subGroupRepository.save(sg);

                    saveSubGroupMember(sg, tm1.getUser());
                    subGroups.add(sg);
                }
            }
        }
    }

    // 8. 결과 DTO 생성
    private MatchingResponseDto buildMatchingResponseDto(Team team, List<SubGroup> subGroups) {
        List<MatchingResultDto> resultDtos = new ArrayList<>();

        for (SubGroup sg : subGroups) {
            List<SubGroupMember> members = subGroupMemberRepository.findBySubGroup_Id(sg.getId());

            List<UserResponseDto> userDtos = members.stream()
                    .map(sgm -> {
                        User user = sgm.getUser();

                        // 유저와 팀 기반으로 TeamMember 정보 조회
                        TeamMember teamMember = teamMemberRepository.findByUser_IdAndTeam_TeamId(user.getId(), team.getTeamId())
                                .orElseThrow(() -> new IllegalArgumentException("TeamMember not found"));

                        return UserResponseDto.builder()
                                .id(user.getId())
                                .name(user.getName())
                                .email(user.getEmail())
                                .gender(user.getGender())
                                .mbti(teamMember.getMbti())  // ✅ 여기에서 MBTI 가져오기
                                .build();
                    })
                    .collect(Collectors.toList());

            resultDtos.add(MatchingResultDto.builder()
                    .subGroupId(sg.getId())
                    .groupName(sg.getName())
                    .members(userDtos)
                    .build());
        }

        return MatchingResponseDto.builder()
                .teamId(team.getTeamId())
                .teamName(team.getTeamName())
                .subGroups(resultDtos)
                .build();
    }


    /**
     * 내부 클래스 PairMatch
     */
    @Data
    @AllArgsConstructor
    static class PairMatch {
        private TeamMember userA;
        private TeamMember userB;
        private double totalScore;
    }


    // 유저 ID 기준으로 기존 Answer 삭제
    // 해당 유저의 기존 Answer를 삭제하고 새 Answer를 저장
    // 새 Answer를 전부 INSERT
    // MatchingService가 매칭 돌릴 때 최신 데이터를 사용
    @Transactional
    public void saveAnswers(AnswerRequestDto dto) {
        Long userId = dto.getUserId();
        Long teamId = dto.getTeamId();

        // 해당 팀의 질문만 필터링해서 삭제
        List<Question> teamQuestions = questionRepository.findByTeam_TeamId(teamId);
        List<Long> teamQuestionIds = teamQuestions.stream()
                .map(Question::getId)
                .collect(Collectors.toList());
        List<Answer> existingAnswers = answerRepository.findByUser_Id(userId).stream()
                .filter(ans -> teamQuestionIds.contains(ans.getQuestion().getId()))
                .collect(Collectors.toList());
        answerRepository.deleteAll(existingAnswers);

        // 새로 저장 << 이게 무슨 뜻이징

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Answer> newAnswers = dto.getAnswers().stream()
                .map(single -> Answer.builder()
                        .user(user)  // 영속 상태의 User 엔티티
                        .question(Question.builder().id(single.getQuestionId()).build())
                        .score(single.getScore())
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
        List<Answer> answers = answerRepository.findByQuestion_Id(questionId);
        // 조회된 Answer 레코드 전부 삭제
        answerRepository.deleteAll(answers);
        // 마지막으로 Question 자체 삭제
        questionRepository.deleteById(questionId);
    }


}
