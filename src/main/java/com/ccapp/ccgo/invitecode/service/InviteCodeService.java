package com.ccapp.ccgo.invitecode.service;

import com.ccapp.ccgo.common.Role;
import com.ccapp.ccgo.common.exception.CustomException;
import com.ccapp.ccgo.invitecode.repository.InviteCodeRepository;
import com.ccapp.ccgo.team.repository.TeamMemberRepository;
import com.ccapp.ccgo.team.repository.TeamRepository;
import com.ccapp.ccgo.team.entity.InviteCode;
import com.ccapp.ccgo.team.entity.Team;
import com.ccapp.ccgo.team.entity.TeamMember;
import org.springframework.scheduling.annotation.Scheduled;
import com.ccapp.ccgo.user.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InviteCodeService {

    private final InviteCodeRepository inviteCodeRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamRepository teamRepository;  // 팀 저장소 추가


    private static final String CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 8;
    private static final SecureRandom random = new SecureRandom();

    //코드 생성기
    private String generateRandomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
        }
        return sb.toString();
    }



    //코드로팀가입
    @Transactional
    public String joinTeamByInviteCode(String code, User user) {

        InviteCode inviteCode = inviteCodeRepository
                .findByCodeAndExpiresAtAfter(code, LocalDateTime.now())
                .orElseThrow(() -> new CustomException("초대 코드가 없거나 만료되었습니다.", HttpStatus.BAD_REQUEST));

        Team team = inviteCode.getTeam();

        boolean alreadyMember = teamMemberRepository.existsByUserAndTeam(user, team);
        if (alreadyMember) {
            throw new CustomException("이미 이 팀에 가입되어 있습니다.", HttpStatus.BAD_REQUEST);
        }
        TeamMember newMember = TeamMember.builder()
                .user(user)
                .team(team)
                .joinedAt(LocalDateTime.now())
                .isActive(true)
                .role(Role.MEMBER)
                .build();

        teamMemberRepository.save(newMember);

        return team.getTeamName();
    }

    //초대코드생성 부분
    @Transactional
    public InviteCode createInviteCode(User user, Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new CustomException("존재하지 않는 팀입니다.", HttpStatus.NOT_FOUND));

        // 팀장이 이 팀의 리더인지 검증
        List<TeamMember> teamMembers = teamMemberRepository.findByUserAndTeamAndIsActiveTrue(user, team);
        boolean isLeader = teamMembers.stream()
                .anyMatch(tm -> tm.getRole() == Role.LEADER);
        if (!isLeader) {
            throw new CustomException("팀장만 초대코드를 생성할 수 있습니다.", HttpStatus.FORBIDDEN);
        }

        // 기존 초대코드 삭제
        inviteCodeRepository.deleteByTeam(team);

        String code;
        do {
            code = generateRandomCode();
        } while (inviteCodeRepository.existsByCode(code));

        InviteCode inviteCode = InviteCode.builder()
                .code(code)
                .team(team)
                .build();

        return inviteCodeRepository.save(inviteCode);
    }


    //현재 시각보다 이전인 초대코드를 삭제
    @Scheduled(fixedRate = 60 * 60 * 1000) // 1시간마다 실행 (ms 단위)
    @Transactional
    public void deleteExpiredInviteCodes() {
        inviteCodeRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }



    @Transactional
    public Team createTeamWithLeader(User user, String teamName) {
        Team team = Team.builder()
                .teamName(teamName)
                .createdBy(user.getId())
                .createdAt(LocalDateTime.now())
                .build();

        Team savedTeam = teamRepository.save(team);

        TeamMember teamLeader = TeamMember.builder()
                .user(user)
                .team(savedTeam)
                .role(Role.LEADER)
                .isActive(true)
                .build();

        teamMemberRepository.save(teamLeader);

        return savedTeam;
    }


}
