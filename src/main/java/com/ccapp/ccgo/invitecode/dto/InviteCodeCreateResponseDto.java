package com.ccapp.ccgo.invitecode.dto;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class InviteCodeCreateResponseDto {
    private String code;
    private LocalDateTime expiresAt;
}
