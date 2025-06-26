package com.ccapp.ccgo.dto;

import lombok.Getter;
import java.time.LocalDate;

@Getter
public class UserRequestDto {
    private String email;
    private String password;
    private String name;
    private String gender;
    private LocalDate birthdate;
}
