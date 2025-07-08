package com.ccapp.ccgo.mission.dto;

import com.ccapp.ccgo.mission.domain.MissionTemplate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MissionTemplateDto {

    private Long id;
    private String title;
    private String description;
    private int score;

    public static MissionTemplateDto fromEntity(MissionTemplate template) {
        return MissionTemplateDto.builder()
                .id(template.getId())
                .title(template.getTitle())
                .description(template.getDescription())
                .score(template.getScore())
                .build();
    }
}
