package com.ccapp.ccgo.mission.dto;

import com.ccapp.ccgo.mission.entity.PartnerMission;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PartnerMissionDto {
    private Long id;
    private String title;
    private String description;
    private LocalDate dueDate;
    private boolean isActive;

    public static PartnerMissionDto fromEntity(PartnerMission pm) {
        return new PartnerMissionDto(
                pm.getId(),
                pm.getTitle(),
                pm.getDescription(),
                pm.getDueDate(),
                pm.isActive()
        );
    }

}