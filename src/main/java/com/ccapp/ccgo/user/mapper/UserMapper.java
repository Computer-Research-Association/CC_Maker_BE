package com.ccapp.ccgo.user.mapper;

import com.ccapp.ccgo.user.dto.UserRequestDto;
import com.ccapp.ccgo.user.dto.UserResponseDto;
import com.ccapp.ccgo.user.dto.UserUpdateRequestDto;
import com.ccapp.ccgo.user.entity.User;

public class UserMapper {

    // RequestDto -> Entity
    public static User toEntity(UserRequestDto dto, String encodedPassword) {
        if (dto == null) return null;

        return User.builder()
                .email(dto.getEmail())
                .password(encodedPassword)
                .name(dto.getName())
                .gender(dto.getGender())
                .birthdate(dto.getBirthdate())
                .build();
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
