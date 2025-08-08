package com.ccapp.ccgo.user.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import jakarta.validation.constraints.*;

@Getter
@Setter
public class UserUpdateRequestDto {
    @NotBlank(message = "이름은 필수입니다.")
    private String name;
    
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;
    
    // 선택적 필드들 (프론트엔드에서 전송하지 않을 수 있음)
    private String birthdate; // YYYY-MM-DD 형식의 문자열
    private String gender;

    // birthdate를 LocalDate로 변환하는 메서드
    public LocalDate getBirthdateAsLocalDate() {
        if (birthdate == null || birthdate.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(birthdate, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("생년월일 형식이 올바르지 않습니다. YYYY-MM-DD 형식으로 입력해주세요.");
        }
    }
}
