package com.ccapp.ccgo;

import com.ccapp.ccgo.User;
import com.ccapp.ccgo.UserService;
import com.ccapp.ccgo.dto.UserResponseDto;
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
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            System.out.println("회원가입 요청 도착: " + user.getEmail());
            UserResponseDto saved = userService.register(user);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            // 예외 메시지 로그 출력
            System.out.println("회원가입 중 오류 발생: " + e.getMessage());
            e.printStackTrace();  // 자세한 에러 추적 정보 출력
            return ResponseEntity.badRequest().body(
                    java.util.Map.of("message", e.getMessage())
            );
        }
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User requestUser) {
        try {
            UserResponseDto loginUser = userService.login(requestUser.getEmail(), requestUser.getPassword());
            return ResponseEntity.ok(loginUser);
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body(java.util.Map.of("message", e.getMessage()));
        }
    }


}
