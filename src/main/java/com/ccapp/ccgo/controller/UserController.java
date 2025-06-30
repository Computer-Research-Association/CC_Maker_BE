package com.ccapp.ccgo.controller;

import com.ccapp.ccgo.service.UserService;
import com.ccapp.ccgo.dto.UserRequestDto;
import com.ccapp.ccgo.dto.UserResponseDto;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*") // 모든 요청 허용 (React Native용)
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody UserRequestDto userRequestDto) {
        UserResponseDto saved = userService.register(userRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
