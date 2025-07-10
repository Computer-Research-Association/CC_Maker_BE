package com.ccapp.ccgo.invitecode.controller;

import com.ccapp.ccgo.invitecode.dto.InviteCodeCreateResponseDto;
import com.ccapp.ccgo.invitecode.dto.InviteCodeJoinRequestDto;
import com.ccapp.ccgo.invitecode.dto.InviteCodeJoinResponseDto;
import com.ccapp.ccgo.team.dto.TeamRequestDto;
import com.ccapp.ccgo.invitecode.repository.InviteCodeRepository;
import com.ccapp.ccgo.team.repository.TeamMemberRepository;
import com.ccapp.ccgo.team.repository.TeamRepository;
import com.ccapp.ccgo.invitecode.service.InviteCodeService;
import com.ccapp.ccgo.team.entity.InviteCode;
import com.ccapp.ccgo.user.entity.User;
import com.ccapp.ccgo.auth.jwt.LoginUserDetails;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

        //작동확인 주석
        if (userDetails != null) {
            System.out.println("인증된 사용자 이메일: " + userDetails.getUsername());
            System.out.println("사용자 권한: " + userDetails.getAuthorities());
        } else {
            System.out.println("userDetails가 null입니다. 인증 정보 없음.");
        }

        User user = userDetails.getUser();
        System.out.print("코드 만듭니당");
        InviteCode inviteCode = inviteCodeService.createInviteCode(user);
        InviteCodeCreateResponseDto responseDto = InviteCodeCreateResponseDto.builder()
                .code(inviteCode.getCode())
                .expiresAt(inviteCode.getExpiresAt())
                .build();

        return ResponseEntity.ok(responseDto);

    }


    //팀 생성하기를 누르면 팀이 만들어집니당
    @PostMapping("/teamname")
    public ResponseEntity<Void> saveTeamName(
            @AuthenticationPrincipal LoginUserDetails userDetails,
            @RequestBody TeamRequestDto requestDto) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userDetails.getUser();
        inviteCodeService.createTeamWithLeader(user, requestDto.getTeamName());

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

        User user = userDetails.getUser();
        System.out.println("1. 컨트롤러 진입");
        String teamName = inviteCodeService.joinTeamByInviteCode(requestDto.getInviteCode(), user);

        return ResponseEntity.ok(new InviteCodeJoinResponseDto(teamName));
    }


}
