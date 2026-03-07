package com.lucerna.backend.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucerna.backend.common.exception.ErrorCode;
import com.lucerna.backend.common.response.CommonResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 인증은 성공했으나 해당 리소스에 대한 권한이 없을 때 (403 Forbidden).
 * 재로그인으로도 해결되지 않는 권한 부족 상황.
 */
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(ErrorCode.AUTH_FORBIDDEN.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), CommonResponse.fail(ErrorCode.AUTH_FORBIDDEN));
    }
}