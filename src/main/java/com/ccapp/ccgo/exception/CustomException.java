package com.ccapp.ccgo.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    //HTTP 상태
    private final HttpStatus status;

    //에러메세지, HTTP 상태 코드 (status)
    public CustomException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }


}
