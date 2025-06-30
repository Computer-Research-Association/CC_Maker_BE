package com.ccapp.ccgo.controller;

import com.ccapp.ccgo.service.UserService;
import com.ccapp.ccgo.dto.UserRequestDto;
import com.ccapp.ccgo.dto.UserResponseDto;
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
}
