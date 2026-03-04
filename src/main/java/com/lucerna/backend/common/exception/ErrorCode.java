package com.lucerna.backend.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 애플리케이션 전체 에러 코드 정의.
 * 각 도메인별 접두사로 구분: AUTH_, COMMON_ 등
 * 새 도메인 추가 시 해당 접두사로 에러 코드를 추가할 것.
 *
 * 형식: DOMAIN_NNN(HttpStatus, "DOMAIN_NNN", "사용자 메시지")
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // === Auth 관련 ===
    AUTH_INVALID_CODE(HttpStatus.BAD_REQUEST, "AUTH_001", "유효하지 않은 인증 코드입니다"),
    AUTH_TOKEN_EXCHANGE_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_002", "토큰 교환에 실패했습니다"),
    AUTH_INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "AUTH_003", "유효하지 않은 Refresh Token입니다"),
    AUTH_UNSUPPORTED_PROVIDER(HttpStatus.BAD_REQUEST, "AUTH_004", "지원하지 않는 소셜 Provider입니다"),

    // === Common ===
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON_001", "입력값이 올바르지 않습니다"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "서버 내부 오류가 발생했습니다");

    private final HttpStatus httpStatus;  // HTTP 응답 상태 코드
    private final String code;            // 클라이언트에 전달되는 에러 코드 문자열
    private final String message;         // 사용자에게 표시할 에러 메시지
}