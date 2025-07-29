package com.ccapp.ccgo.auth.service;

import com.ccapp.ccgo.auth.dto.LoginResponseDto;
import com.ccapp.ccgo.auth.dto.TokenResponseDto;
import com.ccapp.ccgo.auth.jwt.JwtProvider;
import com.ccapp.ccgo.auth.jwt.LoginUserDetails;
import com.ccapp.ccgo.team.entity.TeamMember;
import com.ccapp.ccgo.team.repository.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final TeamMemberRepository teamMemberRepository;
    private final LoginUserDetailsService loginUserDetailsService;

    // 새로 추가: Redis 연동 서비스
    private final RefreshTokenService refreshTokenService;

    public LoginResponseDto login(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        String accessToken = jwtProvider.createAccessToken(authentication);
        String refreshToken = jwtProvider.createRefreshToken(authentication);

        LoginUserDetails userDetails = (LoginUserDetails) authentication.getPrincipal();
        var user = userDetails.getUser();

        // Redis에 Refresh Token 저장 (만료시간: jwt.refresh-token-expiration)
        refreshTokenService.saveRefreshToken(user.getEmail(), refreshToken, jwtProvider.getRefreshTokenExpiration());

        List<TeamMember> teamMembers = teamMemberRepository.findAllByUserAndIsActiveTrue(user);

        List<LoginResponseDto.TeamInfo> teams = teamMembers.stream()
                .map(tm -> LoginResponseDto.TeamInfo.builder()
                        .teamId(tm.getTeam().getTeamId())
                        .teamName(tm.getTeam().getTeamName())
                        .role(tm.getRole().name())
                        .isSurveyCompleted(tm.isSurveyCompleted())
                        .build())
                .toList();

        return LoginResponseDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .teams(teams)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public TokenResponseDto refreshToken(String refreshToken) {
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("리프레시 토큰이 없거나 유효하지 않습니다.");
        }

        String email = jwtProvider.getEmailFromToken(refreshToken);

        // Redis에서 저장된 토큰과 비교
        if (!refreshTokenService.validateRefreshToken(email, refreshToken)) {
            throw new IllegalArgumentException("리프레시 토큰이 만료되었거나 로그아웃 처리된 토큰입니다.");
        }

        LoginUserDetails userDetails = (LoginUserDetails) loginUserDetailsService.loadUserByUsername(email);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );

        String newAccessToken = jwtProvider.createAccessToken(authentication);
        String newRefreshToken = jwtProvider.createRefreshToken(authentication);

        // Redis에 새 Refresh Token 저장 (기존 토큰 덮어쓰기)
        refreshTokenService.saveRefreshToken(email, newRefreshToken, jwtProvider.getRefreshTokenExpiration());

        return new TokenResponseDto(newAccessToken, newRefreshToken);
    }
}
