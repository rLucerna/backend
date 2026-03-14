package com.lucerna.backend.common.exception;

import lombok.Getter;

/**
 * 비즈니스 로직 예외.
 * 서비스 계층에서 비즈니스 규칙 위반 시 throw.
 * GlobalExceptionHandler에서 ErrorCode 기반으로 응답 변환.
 *
 * 사용 예: throw new BusinessException(ErrorCode.AUTH_INVALID_CODE);
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}