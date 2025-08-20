package com.ccapp.ccgo.user.mapper;

import com.ccapp.ccgo.user.dto.UserRequestDto;
import com.ccapp.ccgo.user.dto.UserResponseDto;
import com.ccapp.ccgo.user.dto.UserUpdateRequestDto;
import com.ccapp.ccgo.user.entity.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UserMapper {

    // RequestDto -> Entity
    public static User toEntity(UserRequestDto dto, String encodedPassword) {
        if (dto == null) return null;

        User user = User.builder()
                .email(dto.getEmail())
                .password(encodedPassword)
                .name(dto.getName())
                .gender(dto.getGender())
                .birthdate(dto.getBirthdate())
                .build();
        
        // 개인정보 동의 관련 필드 설정
        if (dto.getPrivacyAgreementVersion() != null) {
            user.setPrivacyAgreementVersion(dto.getPrivacyAgreementVersion());
        }
        user.setPrivacyAgreed(dto.isPrivacyAgreed());
        
        // agreedAt 문자열을 LocalDateTime으로 변환
        if (dto.getPrivacyAgreedAt() != null && !dto.getPrivacyAgreedAt().trim().isEmpty()) {
            try {
                LocalDateTime agreedAt = LocalDateTime.parse(dto.getPrivacyAgreedAt(), 
                    DateTimeFormatter.ISO_DATE_TIME);
                user.setPrivacyAgreedAt(agreedAt);
            } catch (Exception e) {
                // 파싱 실패 시 현재 시간으로 설정
                user.setPrivacyAgreedAt(LocalDateTime.now());
            }
        } else {
            user.setPrivacyAgreedAt(LocalDateTime.now());
        }
        
        if (dto.getPrivacyAgreedMethod() != null) {
            user.setPrivacyAgreedMethod(dto.getPrivacyAgreedMethod());
        }
        if (dto.getPrivacyAgreedEnvironment() != null) {
            user.setPrivacyAgreedEnvironment(dto.getPrivacyAgreedEnvironment());
        }
        
        return user;
    }

    // Entity -> ResponseDto
    public static UserResponseDto toDto(User user) {
        if (user == null) return null;

        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .gender(user.getGender())
                .birthdate(user.getBirthdate())
                .createdAt(user.getCreatedAt())
                .privacyAgreementVersion(user.getPrivacyAgreementVersion())
                .privacyAgreed(user.isPrivacyAgreed())
                .privacyAgreedAt(user.getPrivacyAgreedAt())
                .privacyAgreedMethod(user.getPrivacyAgreedMethod())
                .privacyAgreedEnvironment(user.getPrivacyAgreedEnvironment())
                .build();
    }

    // UpdateRequestDto -> Entity (부분 업데이트용)
    public static void updateEntityFromDto(User user, UserUpdateRequestDto dto) {
        if (user == null || dto == null) return;

        if (dto.getName() != null) {
            user.setName(dto.getName());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getBirthdate() != null && !dto.getBirthdate().trim().isEmpty()) {
            user.setBirthdate(dto.getBirthdateAsLocalDate());
        }
        if (dto.getGender() != null) {
            user.setGender(dto.getGender());
        }
    }
}
