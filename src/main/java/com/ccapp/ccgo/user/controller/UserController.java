package com.ccapp.ccgo.user.controller;

import com.ccapp.ccgo.user.service.UserService;
import com.ccapp.ccgo.user.dto.UserRequestDto;
import com.ccapp.ccgo.user.dto.UserResponseDto;
import com.ccapp.ccgo.user.dto.UserUpdateRequestDto;
import com.ccapp.ccgo.user.dto.PasswordChangeRequestDto;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
// @CrossOrigin 제거, SecurityConfig에서 CORS 관리 권장
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody UserRequestDto userRequestDto) {
        log.info("✅ 회원가입 요청 들어옴: {}", userRequestDto);
        UserResponseDto saved = userService.register(userRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // 현재 로그인한 사용자 정보 조회
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser() {
        log.info("✅ 현재 사용자 정보 조회 요청");
        UserResponseDto user = userService.getCurrentUser();
        return ResponseEntity.ok(user);
    }

    // 사용자 정보 부분 업데이트 (PATCH)
    @PatchMapping("/me")
    public ResponseEntity<UserResponseDto> updateCurrentUser(@Valid @RequestBody UserUpdateRequestDto userUpdateRequestDto) {
        log.info("✅ 사용자 정보 부분 업데이트 요청: {}", userUpdateRequestDto);
        UserResponseDto updated = userService.updateCurrentUser(userUpdateRequestDto);
        return ResponseEntity.ok(updated);
    }

    // 사용자 정보 전체 업데이트 (PUT)
    @PutMapping("/me")
    public ResponseEntity<UserResponseDto> updateCurrentUserFull(@Valid @RequestBody UserUpdateRequestDto userUpdateRequestDto) {
        log.info("✅ 사용자 정보 전체 업데이트 요청: {}", userUpdateRequestDto);
        UserResponseDto updated = userService.updateCurrentUserFull(userUpdateRequestDto);
        return ResponseEntity.ok(updated);
    }

    // 비밀번호 변경
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody PasswordChangeRequestDto passwordChangeRequestDto) {
        log.info("✅ 비밀번호 변경 요청");
        userService.changePassword(passwordChangeRequestDto);
        return ResponseEntity.ok().build();
    }
}
