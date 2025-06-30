package com.ccapp.ccgo.controller;

import com.ccapp.ccgo.dto.InviteCodeCreateResponseDto;
import com.ccapp.ccgo.service.InviteCodeService;
import com.ccapp.ccgo.team.InviteCode;
import com.ccapp.ccgo.user.User;
import com.ccapp.ccgo.jwt.LoginUserDetails;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invitecode")
@RequiredArgsConstructor
public class InviteCodeController {

    private final InviteCodeService inviteCodeService;

    @PostMapping("/create")
    public ResponseEntity<InviteCodeCreateResponseDto> createInviteCode(
            @AuthenticationPrincipal LoginUserDetails userDetails) {

        User user = userDetails.getUser();

        // 초대코드 생성 서비스 호출
        InviteCode inviteCode = inviteCodeService.createInviteCode(user);

        InviteCodeCreateResponseDto responseDto = InviteCodeCreateResponseDto.builder()
                .code(inviteCode.getCode())
                .expiresAt(inviteCode.getExpiresAt())
                .build();

        return ResponseEntity.ok(responseDto);
    }
}
