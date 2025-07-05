package com.ccapp.ccgo.controller;

import com.ccapp.ccgo.common.Role;
import com.ccapp.ccgo.dto.InviteCodeCreateResponseDto;
import com.ccapp.ccgo.dto.InviteCodeJoinRequestDto;
import com.ccapp.ccgo.dto.InviteCodeJoinResponseDto;
import com.ccapp.ccgo.dto.TeamRequestDto;
import com.ccapp.ccgo.exception.CustomException;
import com.ccapp.ccgo.repository.InviteCodeRepository;
import com.ccapp.ccgo.repository.TeamMemberRepository;
import com.ccapp.ccgo.repository.TeamRepository;
import com.ccapp.ccgo.repository.UserRepository;
import com.ccapp.ccgo.service.InviteCodeService;
import com.ccapp.ccgo.team.InviteCode;
import com.ccapp.ccgo.team.Team;
import com.ccapp.ccgo.team.TeamMember;
import com.ccapp.ccgo.user.User;
import com.ccapp.ccgo.jwt.LoginUserDetails;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/invitecode")
@RequiredArgsConstructor
public class InviteCodeController {

    private final InviteCodeService inviteCodeService;
    private final InviteCodeRepository inviteCodeRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamRepository teamRepository;

    //코드 만드는 부분
    @PostMapping("/create")
    public ResponseEntity<InviteCodeCreateResponseDto> createInviteCode(
            @AuthenticationPrincipal LoginUserDetails userDetails) {
        System.out.println("초대코드 생성 요청 들어옴");
        System.out.println("userDetails: " + userDetails);

        if (userDetails != null) {
            System.out.println("인증된 사용자 이메일: " + userDetails.getUsername());
            System.out.println("사용자 권한: " + userDetails.getAuthorities());
        } else {
            System.out.println("userDetails가 null입니다. 인증 정보 없음.");
        }

        User user = userDetails.getUser();

        System.out.print("코드 만듭니당");

        List<TeamMember> teamMembers = teamMemberRepository.findAllByUserAndIsActiveTrue(user);

        TeamMember teamMember = teamMembers.stream()
                .filter(tm -> tm.getRole() == Role.LEADER)
                .findFirst()
                .orElseThrow(() -> new CustomException("팀장만 초대코드를 생성할 수 있습니다.", HttpStatus.FORBIDDEN));

        System.out.println("여까진 됌 ");

        // 초대코드 생성 서비스 호출
        InviteCode inviteCode = inviteCodeService.createInviteCode(user);

        System.out.print("만들었어용");
        InviteCodeCreateResponseDto responseDto = InviteCodeCreateResponseDto.builder()
                .code(inviteCode.getCode())
                .expiresAt(inviteCode.getExpiresAt())
                .build();
        System.out.print("무슨 dto생성해용");
        return ResponseEntity.ok(responseDto);
    }


    //팀 생성하기를 누르면 팀이 만들어집니당
    @PostMapping("/teamname")
    public ResponseEntity<Void> saveTeamName(
            @AuthenticationPrincipal LoginUserDetails userDetails,
            @RequestBody TeamRequestDto requestDto) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();  // 인증 정보 없으면 401 반환
        }
        User user = userDetails.getUser();
        String teamName = requestDto.getTeamName();

        //팀 생성 코드
        // 팀 엔티티 생성 및 저장
        Team team = Team.builder()
                .teamName(teamName)
                .createdBy(user.getId()) // 팀장 ID
                .createdAt(LocalDateTime.now())
                .build();
        teamRepository.save(team);

        // 팀장도 팀원으로 자동 등록
        TeamMember teamMember = TeamMember.builder()
                .team(team)
                .user(user)
                .joinedAt(LocalDateTime.now())
                .isActive(true)
                .role(Role.LEADER) // 팀장 역할
                .build();
        teamMemberRepository.save(teamMember);

        return ResponseEntity.ok().build();
    }

    //팀원이 코드를 보냈으면 처리
    @PostMapping("/join")
    public ResponseEntity<?> joinByInviteCode(
            @RequestBody InviteCodeJoinRequestDto requestDto,
            @AuthenticationPrincipal LoginUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 정보가 없습니다.");
        }

        String code = requestDto.getInviteCode();
        User user = userDetails.getUser();

        // 유효한 초대 코드인지 확인
        InviteCode inviteCode = inviteCodeRepository
                .findByCodeAndExpiresAtAfter(code, LocalDateTime.now())
                .orElseThrow(() -> new RuntimeException("초대 코드가 없거나 만료되었습니다."));

        Team team = inviteCode.getTeam();
        if (team == null) {
            throw new RuntimeException("초대코드에 연결된 팀이 없습니다.");
        }

        boolean alreadyMember = teamMemberRepository.existsByUserAndTeam(user, team);
        if (alreadyMember) {
            return ResponseEntity.badRequest().body("이미 이 팀에 가입되어 있습니다.");
        }

        // 새 TeamMember 생성 및 저장
        TeamMember newMember = TeamMember.builder()
                .user(user)
                .team(team)
                .joinedAt(LocalDateTime.now())
                .isActive(true)
                .role(Role.MEMBER) // 기본 역할로 MEMBER 지정, 필요하면 변경
                .build();
        teamMemberRepository.save(newMember);


        return ResponseEntity.ok(new InviteCodeJoinResponseDto(team.getTeamName()));
    }


}
