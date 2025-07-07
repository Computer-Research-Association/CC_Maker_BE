package com.ccapp.ccgo.mission.domain;

import com.ccapp.ccgo.common.MissionStatus;
import com.ccapp.ccgo.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserMission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 미션을 수행하는 유저
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // 짝 미션
    @ManyToOne
    @JoinColumn(name = "partner_mission_id")
    private PartnerMission partnerMission;

    // 미션 상태 (예: PENDING, COMPLETE)
    @Enumerated(EnumType.STRING)
    private MissionStatus status;

    // 미션 제출 시간
    private LocalDateTime submittedAt;
}