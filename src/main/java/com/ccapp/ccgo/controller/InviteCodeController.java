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
}
