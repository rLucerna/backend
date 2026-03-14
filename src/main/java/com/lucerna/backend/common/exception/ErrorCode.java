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
    AUTH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_005", "액세스 토큰이 만료되었습니다. 토큰을 갱신해주세요"),
    AUTH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "AUTH_006", "유효하지 않은 토큰입니다"),
    AUTH_FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH_007", "접근 권한이 없습니다"),

    // === User 관련 ===
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "사용자를 찾을 수 없습니다"),
    USER_EMAIL_NOT_FOUND(HttpStatus.BAD_REQUEST, "USER_002", "JWT에 이메일 정보가 포함되어 있지 않습니다"),

    // === Common ===
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON_001", "입력값이 올바르지 않습니다"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "서버 내부 오류가 발생했습니다");

    private final HttpStatus httpStatus;  // HTTP 응답 상태 코드
    private final String code;            // 클라이언트에 전달되는 에러 코드 문자열
    private final String message;         // 사용자에게 표시할 에러 메시지
}