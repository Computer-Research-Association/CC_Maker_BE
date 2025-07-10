package com.ccapp.ccgo.mission.dto;

import com.ccapp.ccgo.common.MissionStatus;
import com.ccapp.ccgo.mission.entity.UserMission;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserMissionDto {
    private Long id;
    private Long userId;
    private Long partnerMissionId;
    private MissionStatus status;
    private LocalDateTime submittedAt;

    public static UserMissionDto fromEntity(UserMission um) {
        return new UserMissionDto(
                um.getId(),
                um.getUser().getId(),
                um.getPartnerMission().getId(),
                um.getStatus(),
                um.getSubmittedAt()
        );
    }
}