package com.ccapp.ccgo.common.test;

import com.ccapp.ccgo.user.dto.UserUpdateRequestDto;
import com.ccapp.ccgo.user.dto.PasswordChangeRequestDto;
import com.ccapp.ccgo.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/test/user")
@RequiredArgsConstructor
public class UserApiTestController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<?> testGetCurrentUser() {
        log.info("🧪 테스트: 현재 사용자 정보 조회");
        try {
            var result = userService.getCurrentUser();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("❌ 테스트 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body("테스트 실패: " + e.getMessage());
        }
    }

    @PatchMapping("/me")
    public ResponseEntity<?> testUpdateCurrentUser(@RequestBody UserUpdateRequestDto dto) {
        log.info("🧪 테스트: 사용자 정보 부분 업데이트 - {}", dto);
        try {
            var result = userService.updateCurrentUser(dto);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("❌ 테스트 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body("테스트 실패: " + e.getMessage());
        }
    }

    @PutMapping("/me")
    public ResponseEntity<?> testUpdateCurrentUserFull(@RequestBody UserUpdateRequestDto dto) {
        log.info("🧪 테스트: 사용자 정보 전체 업데이트 - {}", dto);
        try {
            var result = userService.updateCurrentUserFull(dto);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("❌ 테스트 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body("테스트 실패: " + e.getMessage());
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> testChangePassword(@RequestBody PasswordChangeRequestDto dto) {
        log.info("🧪 테스트: 비밀번호 변경");
        try {
            userService.changePassword(dto);
            return ResponseEntity.ok("비밀번호 변경 성공");
        } catch (Exception e) {
            log.error("❌ 테스트 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body("테스트 실패: " + e.getMessage());
        }
    }
}
