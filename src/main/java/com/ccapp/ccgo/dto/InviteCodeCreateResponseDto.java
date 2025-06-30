package com.ccapp.ccgo.dto;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class InviteCodeCreateResponseDto {
    private String code;
    private LocalDateTime expiresAt;
}
