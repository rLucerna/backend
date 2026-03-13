package com.lucerna.backend.common.exception;

import com.lucerna.backend.common.response.CommonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 전역 예외 처리기.
 * 모든 컨트롤러에서 발생하는 예외를 CommonResponse 형태로 통일하여 반환.
 * - BusinessException: 비즈니스 로직 오류 (ErrorCode 기반)
 * - MethodArgumentNotValidException: @Valid 검증 실패
 * - Exception: 예상치 못한 서버 오류 (catch-all)
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 비즈니스 로직 예외 → ErrorCode에 정의된 HTTP 상태 코드로 응답 */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<CommonResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("BusinessException: code={}, message={}", e.getErrorCode().getCode(), e.getMessage());
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(CommonResponse.fail(errorCode));
    }

    /** @Valid 검증 실패 → 400 Bad Request + 필드 에러 메시지 로깅 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String fields = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("ValidationException: {}", fields);
        return ResponseEntity
                .badRequest()
                .body(CommonResponse.fail(ErrorCode.INVALID_INPUT));
    }

    /** 처리되지 않은 모든 예외 → 500 Internal Server Error */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<Void>> handleException(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseEntity
                .internalServerError()
                .body(CommonResponse.fail(ErrorCode.INTERNAL_ERROR));
    }
}