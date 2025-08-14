package com.ccapp.ccgo.invitecode.controller;

import com.ccapp.ccgo.invitecode.dto.InviteCodeCreateRequestDto;
import com.ccapp.ccgo.invitecode.dto.InviteCodeCreateResponseDto;
import com.ccapp.ccgo.invitecode.dto.InviteCodeJoinRequestDto;
import com.ccapp.ccgo.invitecode.dto.InviteCodeJoinResponseDto;
import com.ccapp.ccgo.team.dto.TeamRequestDto;
import com.ccapp.ccgo.invitecode.repository.InviteCodeRepository;
import com.ccapp.ccgo.team.dto.TeamResponseDto;
import com.ccapp.ccgo.team.entity.Team;
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
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/invitecode")
@RequiredArgsConstructor
@Slf4j
public class InviteCodeController {

    private final InviteCodeService inviteCodeService;
    private final InviteCodeRepository inviteCodeRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamRepository teamRepository;

    /**
     * 초대코드 생성
     */
    @PostMapping("/create")
    public ResponseEntity<InviteCodeCreateResponseDto> createInviteCode(
            @AuthenticationPrincipal LoginUserDetails userDetails,
            @RequestBody InviteCodeCreateRequestDto requestDto) {

        log.info("[InviteCode] 초대코드 생성 요청 | user: {}", userDetails.getUsername());

        User user = userDetails.getUser();
        Long teamId = requestDto.getTeamId();

        InviteCode inviteCode = inviteCodeService.createInviteCode(user, teamId);

        InviteCodeCreateResponseDto responseDto = InviteCodeCreateResponseDto.builder()
                .code(inviteCode.getCode())
                .expiresAt(inviteCode.getExpiresAt())
                .build();

        log.info("[InviteCode] 초대코드 생성 완료 | code: {}", inviteCode.getCode());

        return ResponseEntity.ok(responseDto);
    }


    /**
     * 팀 생성
     */
    @PostMapping("/teamname")
    public ResponseEntity<TeamResponseDto> saveTeamName(
            @AuthenticationPrincipal LoginUserDetails userDetails,
            @RequestBody TeamRequestDto requestDto) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userDetails.getUser();

        Team team = inviteCodeService.createTeamWithLeader(user, requestDto.getTeamName());

        TeamResponseDto responseDto = TeamResponseDto.builder()
                .teamId(team.getTeamId())
                .teamName(team.getTeamName())
                .build();

        log.info("[InviteCode] 팀 생성 완료 | teamId: {}", team.getTeamId());

        return ResponseEntity.ok(responseDto);
    }


    /**
     * 초대코드로 팀 가입
     */
    @PostMapping("/join")
    public ResponseEntity<?> joinByInviteCode(
            @RequestBody InviteCodeJoinRequestDto requestDto,
            @AuthenticationPrincipal LoginUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 정보가 없습니다.");
        }

        User user = userDetails.getUser();
        log.info("[InviteCode] 팀 가입 요청 | user: {}, code: {}", user.getEmail(), requestDto.getInviteCode());
        String teamName = inviteCodeService.joinTeamByInviteCode(requestDto.getInviteCode(), user);

        log.info("[InviteCode] 팀 가입 완료 | user: {}, team: {}", user.getEmail(), teamName);
        return ResponseEntity.ok(new InviteCodeJoinResponseDto(teamName));
    }


}
