package com.ccapp.ccgo.user.dto;

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
    
    // 개인정보 동의 관련 필드들
    private String privacyAgreementVersion;
    private boolean privacyAgreed;
    private String privacyAgreedAt;
    private String privacyAgreedMethod;
    private String privacyAgreedEnvironment;
}
