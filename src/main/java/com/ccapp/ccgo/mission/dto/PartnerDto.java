package com.ccapp.ccgo.mission.dto;

import com.ccapp.ccgo.mission.domain.Partner;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PartnerDto {

    private Long id;
    private Long teamId;
    private Long user1Id;
    private Long user2Id;

    public static PartnerDto fromEntity(Partner partner) {
        return PartnerDto.builder()
                .id(partner.getId())
                .teamId(partner.getTeam().getTeamId())
                .user1Id(partner.getUser1().getId())
                .user2Id(partner.getUser2().getId())
                .build();
    }
}
