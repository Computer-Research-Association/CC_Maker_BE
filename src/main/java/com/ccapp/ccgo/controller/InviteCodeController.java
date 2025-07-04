package com.ccapp.ccgo.controller;

import com.ccapp.ccgo.common.Role;
import com.ccapp.ccgo.dto.InviteCodeCreateResponseDto;
import com.ccapp.ccgo.dto.InviteCodeJoinRequestDto;
import com.ccapp.ccgo.dto.InviteCodeJoinResponseDto;
import com.ccapp.ccgo.dto.TeamRequestDto;
import com.ccapp.ccgo.repository.InviteCodeRepository;
import com.ccapp.ccgo.repository.TeamMemberRepository;
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

@RestController
@RequestMapping("/api/invitecode")
@RequiredArgsConstructor
public class InviteCodeController {

    private final InviteCodeService inviteCodeService;
    private final InviteCodeRepository inviteCodeRepository;
    private final TeamMemberRepository teamMemberRepository;

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
        System.out.println("ROLE: " + user.getRole());

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


    //시작하기를 누르면 팀 이름을 db에 저장
    @PostMapping("/teamname")
    public ResponseEntity<Void> saveTeamName(
            @AuthenticationPrincipal LoginUserDetails userDetails,
            @RequestBody TeamRequestDto requestDto) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();  // 인증 정보 없으면 401 반환
        }
        User user = userDetails.getUser();
        String teamName = requestDto.getTeamName();
        // 팀 이름 저장 서비스 호출
        inviteCodeService.saveTeamName(user, teamName);
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

        // 이미 등록된 TeamMember 가져오기
        TeamMember teamMember = teamMemberRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("TeamMember 정보가 없습니다."));

        // 이미 팀이 설정되어 있다면 중복 가입 방지
        if (teamMember.getTeam() != null) {
            return ResponseEntity.badRequest().body("이미 다른 팀에 가입되어 있습니다.");
        }

        // 팀 할당 및 기타 정보 설정
        teamMember.setTeam(team);
        teamMember.setRole(user.getRole()); // 유저의 역할로 설정
        teamMember.setJoinedAt(LocalDateTime.now());
        teamMember.setActive(true);
        teamMemberRepository.save(teamMember); // 업데이트 저장

        return ResponseEntity.ok(new InviteCodeJoinResponseDto(team.getTeamName()));
    }


}
