package com.ccapp.ccgo.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class UserResponseDto {
    private Long id;
    private String email;
    private String name;
    private String gender;
    private LocalDate birthdate;
    private LocalDateTime createdAt;
}

