package com.ccapp.ccgo.controller;

import com.ccapp.ccgo.dto.TeamMemberResponseDto;
import com.ccapp.ccgo.repository.TeamMemberRepository;
import com.ccapp.ccgo.team.TeamMember;
import com.ccapp.ccgo.user.User;
import com.ccapp.ccgo.team.Team;
import com.ccapp.ccgo.dto.LoginRequestDto;
import com.ccapp.ccgo.dto.LoginResponseDto;
import com.ccapp.ccgo.jwt.JwtProvider;
import com.ccapp.ccgo.jwt.LoginUserDetails;
import lombok.extern.slf4j.Slf4j;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final TeamMemberRepository teamMemberRepository;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto requestDto) {
        log.info("로그인 요청 받음: {}", requestDto.getEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            requestDto.getEmail(), requestDto.getPassword()
                    )
            );

            String accessToken = jwtProvider.createAccessToken(authentication);
            String refreshToken = jwtProvider.createRefreshToken(authentication);
            LoginUserDetails userDetails = (LoginUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser(); // ✅ 안전하게 User 꺼냄

            TeamMember teamMember = teamMemberRepository.findByUserAndIsActiveTrue(user)
                    .orElseThrow(() -> new RuntimeException("소속된 팀이 없습니다."));

            LoginResponseDto response = LoginResponseDto.builder()
                    .grantType("Bearer")
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .userId(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .teamId(teamMember.getTeam().getTeamId())   //필요한 정보일까
                    .teamName(teamMember.getTeam().getTeamName())   //필요한 정보일까
                    .role(teamMember.getRole())
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("로그인 중 오류 발생", e);
            throw e;
        }
    }
}

