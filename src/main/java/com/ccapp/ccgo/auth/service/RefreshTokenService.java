package com.ccapp.ccgo.auth.service;

import com.ccapp.ccgo.auth.entity.RefreshToken;
import com.ccapp.ccgo.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public void saveRefreshToken(String email, String refreshToken, long expirationMillis) {
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expirationMillis / 1000);
        
        RefreshToken token = RefreshToken.builder()
                .email(email)
                .refreshToken(refreshToken)
                .expiresAt(expiresAt)
                .build();
        
        refreshTokenRepository.save(token);
        log.info("Refresh Token 저장 완료: {}", email);
    }

    public String getRefreshToken(String email) {
        return refreshTokenRepository.findByEmail(email)
                .map(RefreshToken::getRefreshToken)
                .orElse(null);
    }

    public boolean validateRefreshToken(String email, String refreshToken) {
        return refreshTokenRepository.findByEmail(email)
                .map(token -> token.getRefreshToken().equals(refreshToken) && 
                             token.getExpiresAt().isAfter(LocalDateTime.now()))
                .orElse(false);
    }

    public void deleteRefreshToken(String email) {
        refreshTokenRepository.deleteByEmail(email);
        log.info("Refresh Token 삭제 완료: {}", email);
    }

    public void updateRefreshToken(String email, String newRefreshToken, long expirationMillis) {
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expirationMillis / 1000);
        
        RefreshToken token = RefreshToken.builder()
                .email(email)
                .refreshToken(newRefreshToken)
                .expiresAt(expiresAt)
                .build();
        
        refreshTokenRepository.save(token);
        log.info("Refresh Token 업데이트 완료: {}", email);
    }
}
