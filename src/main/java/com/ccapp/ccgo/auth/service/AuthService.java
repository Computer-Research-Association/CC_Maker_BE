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
    private final RefreshTokenService refreshTokenService;

    public LoginResponseDto login(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        String accessToken = jwtProvider.createAccessToken(authentication);
        String refreshToken = jwtProvider.createRefreshToken(authentication);

        LoginUserDetails userDetails = (LoginUserDetails) authentication.getPrincipal();
        var user = userDetails.getUser();

        // Refresh Token DB 저장
        refreshTokenService.saveRefreshToken(email, refreshToken, jwtProvider.getRefreshTokenExpiration());

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

    public TokenResponseDto refreshToken(String accessToken, String refreshToken) {
        // 1. Access Token 구조 검증 (만료는 허용)
        if (!jwtProvider.validateTokenStructure(accessToken)) {
            throw new IllegalArgumentException("유효하지 않은 Access Token 구조입니다.");
        }

        // 2. Refresh Token 검증
        if (!jwtProvider.validateRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }

        // 3. DB의 Refresh Token과 비교
        String email = jwtProvider.getEmailFromToken(refreshToken);
        if (!refreshTokenService.validateRefreshToken(email, refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }

        // 4. 새로운 토큰 생성
        LoginUserDetails userDetails = (LoginUserDetails) loginUserDetailsService.loadUserByUsername(email);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
        );

        String newAccessToken = jwtProvider.createAccessToken(authentication);
        String newRefreshToken = jwtProvider.createRefreshToken(authentication);

        // 5. Refresh Token Rotation (기존 토큰 삭제, 새로운 토큰 저장)
        refreshTokenService.updateRefreshToken(email, newRefreshToken, jwtProvider.getRefreshTokenExpiration());

        return new TokenResponseDto(newAccessToken, newRefreshToken);
    }
}
