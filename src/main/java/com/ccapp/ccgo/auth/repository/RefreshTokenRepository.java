package com.ccapp.ccgo.auth.repository;

import com.ccapp.ccgo.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    
    Optional<RefreshToken> findByEmail(String email);
    
    void deleteByEmail(String email);
    
    boolean existsByEmail(String email);
}
