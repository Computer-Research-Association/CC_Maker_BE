package com.ccapp.ccgo.dto;

import com.ccapp.ccgo.common.Role;
import lombok.Getter;
import java.time.LocalDate;
import jakarta.validation.constraints.*;

@Getter
public class UserRequestDto {
    @NotNull
    @Email
    private String email;

    @NotNull
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    private String password;
    private String name;
    private String gender;
    private LocalDate birthdate;
    private Role role;
}
