package com.ccapp.ccgo.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.context.support.DefaultMessageSourceResolvable;

import java.util.HashMap;
import java.util.stream.Collectors;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;

import java.util.Map;
@Slf4j
@RestControllerAdvice
//다양한 에러 처리 클래스
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Map<String, String>> handleCustomException(CustomException ex) {
        return ResponseEntity
                .status(ex.getStatus()) // 예외에서 상태코드 가져오기
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception ex) {
        log.error("서버 오류 발생", ex);
        return ResponseEntity
                .status(500)
                .body(Map.of("message", "서버 오류가 발생했습니다."));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(Map.of("message", errorMessage));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "이메일 또는 비밀번호가 잘못되었습니다."));
    }


    @ExceptionHandler(MatchingAlreadyCompletedException.class)
    public ResponseEntity<Map<String, String>> handleMatchingAlreadyStarted(MatchingAlreadyCompletedException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(InvalidDataAccessResourceUsageException.class)
    public ResponseEntity<Map<String, String>> handleTableNotExists(InvalidDataAccessResourceUsageException ex) {
        log.warn("테이블이 존재하지 않는 오류: {}", ex.getMessage());
        // 테이블이 존재하지 않는 경우 200 OK와 빈 리스트를 반환하도록 프론트엔드에서 처리
        return ResponseEntity.ok(Map.of("message", "데이터를 조회할 수 없습니다."));
    }
}
