package com.ccapp.ccgo.dto;

import com.ccapp.ccgo.user.User;

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
}
