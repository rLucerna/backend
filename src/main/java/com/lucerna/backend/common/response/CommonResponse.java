package com.lucerna.backend.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lucerna.backend.common.exception.ErrorCode;
import lombok.Getter;

/**
 * 공통 API 응답 래퍼.
 * 모든 API 응답은 이 형태로 통일.
 *
 * 성공: { "success": true, "data": {...} }
 * 실패: { "success": false, "error": { "code": "...", "message": "..." } }
 *
 * null 필드는 JSON에서 제외됨 (@JsonInclude NON_NULL)
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonResponse<T> {

    private final boolean success;
    private final T data;
    private final ErrorInfo error;

    private CommonResponse(boolean success, T data, ErrorInfo error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }

    /** 데이터를 포함한 성공 응답 */
    public static <T> CommonResponse<T> success(T data) {
        return new CommonResponse<>(true, data, null);
    }

    /** 데이터 없는 성공 응답 (예: DELETE 처리 완료) */
    public static <T> CommonResponse<T> success() {
        return new CommonResponse<>(true, null, null);
    }

    /** ErrorCode 기반 실패 응답 */
    public static <T> CommonResponse<T> fail(ErrorCode errorCode) {
        return new CommonResponse<>(false, null, new ErrorInfo(errorCode.getCode(), errorCode.getMessage()));
    }

    @Getter
    public static class ErrorInfo {
        private final String code;
        private final String message;

        public ErrorInfo(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}