package com.ccapp.ccgo.user.repository;

import com.ccapp.ccgo.user.entity.PrivacyAgreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PrivacyAgreementRepository extends JpaRepository<PrivacyAgreement, Long> {
    
    // 버전으로 동의서 조회
    Optional<PrivacyAgreement> findByVersion(String version);
    
    // 현재 활성화된 동의서 조회
    Optional<PrivacyAgreement> findByIsActiveTrue();
    
    // 특정 버전이 존재하는지 확인
    boolean existsByVersion(String version);
    
    // 최신 버전 조회
    @Query("SELECT pa FROM PrivacyAgreement pa WHERE pa.isActive = true ORDER BY pa.createdAt DESC")
    Optional<PrivacyAgreement> findLatestActive();
}
