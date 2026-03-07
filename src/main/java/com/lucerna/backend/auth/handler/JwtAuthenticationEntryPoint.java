package com.lucerna.backend.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucerna.backend.common.exception.ErrorCode;
import com.lucerna.backend.common.response.CommonResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JWT 인증 실패 시 공통 응답 반환.
 * - 만료된 토큰 (AUTH_005): 클라이언트가 /auth/refresh를 호출해야 함
 * - 유효하지 않은 토큰 (AUTH_006): 재로그인 필요
 * - 토큰 없음 등 기타 (AUTH_006): 재로그인 필요
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        ErrorCode errorCode = resolveErrorCode(authException);

        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), CommonResponse.fail(errorCode));
    }

    /**
     * 만료된 토큰과 유효하지 않은 토큰을 구분.
     * InvalidBearerTokenException → JwtValidationException → OAuth2Error "expired" 포함 여부로 판단.
     */
    private ErrorCode resolveErrorCode(AuthenticationException authException) {
        if (authException instanceof InvalidBearerTokenException ibte
                && ibte.getCause() instanceof JwtValidationException jve) {
            boolean isExpired = jve.getErrors().stream()
                    .anyMatch(e -> e.getDescription() != null
                            && e.getDescription().contains("expired"));
            return isExpired ? ErrorCode.AUTH_TOKEN_EXPIRED : ErrorCode.AUTH_TOKEN_INVALID;
        }
        return ErrorCode.AUTH_TOKEN_INVALID;
    }
}