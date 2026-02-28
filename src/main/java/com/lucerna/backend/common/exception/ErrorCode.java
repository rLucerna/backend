package com.lucerna.backend.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    //아래 선언된 상수는 private ErrorCode(HttpStatus httpStatus, String code, String message)와 동일.
    // Auth
    AUTH_INVALID_CODE(HttpStatus.BAD_REQUEST, "AUTH_001", "유효하지 않은 인증 코드입니다"),
    AUTH_TOKEN_EXCHANGE_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_002", "토큰 교환에 실패했습니다"),
    AUTH_INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "AUTH_003", "유효하지 않은 Refresh Token입니다"),
    AUTH_UNSUPPORTED_PROVIDER(HttpStatus.BAD_REQUEST, "AUTH_004", "지원하지 않는 소셜 Provider입니다"),

    // Common
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON_001", "입력값이 올바르지 않습니다"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "서버 내부 오류가 발생했습니다");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
