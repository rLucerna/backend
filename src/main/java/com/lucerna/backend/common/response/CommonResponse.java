package com.lucerna.backend.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lucerna.backend.common.exception.ErrorCode;
import lombok.Getter;

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

    public static <T> CommonResponse<T> success(T data) {
        return new CommonResponse<>(true, data, null);
    }

    public static <T> CommonResponse<T> success() {
        return new CommonResponse<>(true, null, null);
    }

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
