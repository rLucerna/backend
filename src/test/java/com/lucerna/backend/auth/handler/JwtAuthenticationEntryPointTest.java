package com.lucerna.backend.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucerna.backend.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthenticationEntryPointTest {

    private JwtAuthenticationEntryPoint entryPoint;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        entryPoint = new JwtAuthenticationEntryPoint(objectMapper);
    }

    @Test
    @DisplayName("만료된 토큰 → 401 + AUTH_005")
    void 만료된_토큰_AUTH_005() throws Exception {
        OAuth2Error expiredError = new OAuth2Error("invalid_token",
                "Jwt expired at 2024-01-01T00:00:00Z", null);
        JwtValidationException jwtEx = new JwtValidationException("Jwt expired", List.of(expiredError));
        InvalidBearerTokenException exception = new InvalidBearerTokenException("expired", jwtEx);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, exception);

        assertThat(response.getStatus()).isEqualTo(401);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = objectMapper.readValue(response.getContentAsString(), Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> error = (Map<String, Object>) body.get("error");

        assertThat(body.get("success")).isEqualTo(false);
        assertThat(error.get("code")).isEqualTo(ErrorCode.AUTH_TOKEN_EXPIRED.getCode());
    }

    @Test
    @DisplayName("서명이 유효하지 않은 토큰 → 401 + AUTH_006")
    void 무효한_토큰_AUTH_006() throws Exception {
        OAuth2Error invalidError = new OAuth2Error("invalid_token", "Signed JWT rejected", null);
        JwtValidationException jwtEx = new JwtValidationException("Invalid JWT", List.of(invalidError));
        InvalidBearerTokenException exception = new InvalidBearerTokenException("invalid", jwtEx);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, exception);

        assertThat(response.getStatus()).isEqualTo(401);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = objectMapper.readValue(response.getContentAsString(), Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> error = (Map<String, Object>) body.get("error");

        assertThat(body.get("success")).isEqualTo(false);
        assertThat(error.get("code")).isEqualTo(ErrorCode.AUTH_TOKEN_INVALID.getCode());
    }

    @Test
    @DisplayName("토큰 없음 등 기타 인증 실패 → 401 + AUTH_006")
    void 기타_인증_실패_AUTH_006() throws Exception {
        InvalidBearerTokenException exception = new InvalidBearerTokenException("no token");

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, exception);

        assertThat(response.getStatus()).isEqualTo(401);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = objectMapper.readValue(response.getContentAsString(), Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> error = (Map<String, Object>) body.get("error");

        assertThat(body.get("success")).isEqualTo(false);
        assertThat(error.get("code")).isEqualTo(ErrorCode.AUTH_TOKEN_INVALID.getCode());
    }
}