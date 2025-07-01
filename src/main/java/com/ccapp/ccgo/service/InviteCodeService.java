package com.ccapp.ccgo.service;

import com.ccapp.ccgo.exception.CustomException;
import com.ccapp.ccgo.repository.InviteCodeRepository;
import com.ccapp.ccgo.repository.TeamMemberRepository;
import com.ccapp.ccgo.team.InviteCode;
import com.ccapp.ccgo.team.Team;
import com.ccapp.ccgo.team.TeamMember;
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

        if (teamMemberRepository.findByUserAndIsActiveTrue(user).isPresent()) {
            throw new CustomException("이미 팀에 소속되어 있습니다.", HttpStatus.BAD_REQUEST);
        }

        TeamMember newMember = TeamMember.builder()
                .user(user)
                .team(code.getTeam())
                .role("TEAM_MEMBER")
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
        if (!"TeamLeader".equals(teamMember.getRole())) {
            throw new CustomException("팀장만 초대코드를 생성할 수 있습니다.", HttpStatus.FORBIDDEN);
        }

        Team team = teamMember.getTeam();

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

    //
}
