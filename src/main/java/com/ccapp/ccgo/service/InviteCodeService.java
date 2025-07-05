package com.ccapp.ccgo.service;

import com.ccapp.ccgo.common.Role;
import com.ccapp.ccgo.exception.CustomException;
import com.ccapp.ccgo.repository.InviteCodeRepository;
import com.ccapp.ccgo.repository.TeamMemberRepository;
import com.ccapp.ccgo.repository.TeamRepository;
import com.ccapp.ccgo.team.InviteCode;
import com.ccapp.ccgo.team.Team;
import com.ccapp.ccgo.team.TeamMember;
import lombok.NonNull;
import org.springframework.scheduling.annotation.Scheduled;
import com.ccapp.ccgo.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

import java.security.SecureRandom;

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
    public void joinTeamByInviteCode(User user, String inviteCode) {
        InviteCode code = inviteCodeRepository.findById(inviteCode)
                .orElseThrow(() -> new CustomException("초대코드가 유효하지 않습니다.", HttpStatus.BAD_REQUEST));

        if (code.isExpired()) {
            inviteCodeRepository.delete(code); // 만료된 코드는 삭제
            throw new CustomException("초대코드가 만료되었습니다.", HttpStatus.BAD_REQUEST);
        }

        Team teamToJoin = code.getTeam();

        // 유저가 이미 이 팀에 가입했는지 체크
        boolean alreadyJoined = teamMemberRepository.findAllByUserAndIsActiveTrue(user).stream()
                .anyMatch(tm -> tm.getTeam().equals(teamToJoin));
        if (alreadyJoined) {
            throw new CustomException("이미 이 팀에 가입되어 있습니다.", HttpStatus.BAD_REQUEST);
        }

        TeamMember newMember = TeamMember.builder()
                .user(user)
                .team(teamToJoin)
                .role(Role.MEMBER)  // 기본 역할은 MEMBER
                .isActive(true)
                .joinedAt(LocalDateTime.now())
                .build();

        teamMemberRepository.save(newMember);
    }

    //보안상 냅둬
    @Transactional
    public InviteCode createInviteCode(User user) {
        var teamMember = teamMemberRepository.findByUserAndIsActiveTrue(user)
                .orElseThrow(() -> new CustomException("팀 소속이 아닙니다.", HttpStatus.BAD_REQUEST));
        if (teamMember.getRole() != Role.LEADER) {
            throw new CustomException("팀장만 초대코드를 생성할 수 있습니다.", HttpStatus.FORBIDDEN);
        }

        Team team = teamMember.getTeam();
        //기존 코드 삭제
        inviteCodeRepository.deleteByTeam(team);

        String code;
        do {
            code = generateRandomCode();
        } while (inviteCodeRepository.existsById(code));

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
    public void saveTeamName(@NonNull User user, @NonNull String teamName) {
        // 사용자 팀 멤버 조회
        TeamMember teamMember = teamMemberRepository.findByUserAndIsActiveTrue(user)
                .orElseThrow(() -> new CustomException("팀 소속이 아닙니다.", HttpStatus.BAD_REQUEST));

        // 팀장만 팀 이름 변경 가능 (원한다면 이 조건 제거 가능)
        if (teamMember.getRole() != Role.LEADER) {
            throw new CustomException("팀장만 팀 이름을 변경할 수 있습니다.", HttpStatus.FORBIDDEN);
        }

        Team team = teamMember.getTeam();
        team.setTeamName(teamName);  // 팀 이름 변경
        teamRepository.save(team);
    }
}
