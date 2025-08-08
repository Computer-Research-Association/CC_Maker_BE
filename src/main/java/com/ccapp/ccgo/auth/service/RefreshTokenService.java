package com.ccapp.ccgo.auth.service;

import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService {

    /**
     * Stateless 방식이므로 저장 기능은 필요하지 않습니다.
     */
    public void saveRefreshToken(String email, String refreshToken, long expirationMillis) {
        // 저장 기능 제거
    }

    /**
     * Stateless 방식이므로 조회 기능은 필요하지 않습니다.
     */
    public String getRefreshToken(String email) {
        return null;
    }

    /**
     * JWT 토큰 자체를 검증하므로 여기서는 항상 true 리턴하거나 별도 검증을 하지 않습니다.
     * 실제 토큰 검증은 JwtProvider에서 수행됩니다.
     */
    public boolean validateRefreshToken(String email, String refreshToken) {
        return true;
    }

    /**
     * 서버에 저장된 토큰이 없으므로 삭제할 것도 없습니다.
     */
    public void deleteRefreshToken(String email) {
        // 삭제 기능 제거
    }
}
