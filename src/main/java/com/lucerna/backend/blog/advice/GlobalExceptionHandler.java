package com.lucerna.backend.blog.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice // "식당(서버)에서 터지는 모든 에러는 내가 다 수습할게!"
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        // 누군가 IllegalArgumentException을 던지면,
        // 500 에러 대신 409 Conflict(충돌) 상태 코드와 함께 에러 메시지를 반환해!
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
}