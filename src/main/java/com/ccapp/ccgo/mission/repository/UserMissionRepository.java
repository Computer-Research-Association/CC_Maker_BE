package com.ccapp.ccgo.mission.repository;

import com.ccapp.ccgo.mission.entity.UserMission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserMissionRepository extends JpaRepository<UserMission, Long> {
    List<UserMission> findByUserId(Long userId);
    List<UserMission> findByPartnerMissionId(Long partnerMissionId);
}