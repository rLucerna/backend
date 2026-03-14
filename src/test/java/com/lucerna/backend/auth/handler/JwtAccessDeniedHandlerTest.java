package com.lucerna.backend.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucerna.backend.common.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAccessDeniedHandlerTest {

    private JwtAccessDeniedHandler handler;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        handler = new JwtAccessDeniedHandler(objectMapper);
    }

    @Test
    @DisplayName("권한 없는 접근 → 403 + AUTH_007")
    void 권한_없는_접근_AUTH_007() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AccessDeniedException exception = new AccessDeniedException("Access Denied");

        handler.handle(request, response, exception);

        assertThat(response.getStatus()).isEqualTo(403);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = objectMapper.readValue(response.getContentAsString(), Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> error = (Map<String, Object>) body.get("error");

        assertThat(body.get("success")).isEqualTo(false);
        assertThat(error.get("code")).isEqualTo(ErrorCode.AUTH_FORBIDDEN.getCode());
        assertThat(error.get("message")).isEqualTo(ErrorCode.AUTH_FORBIDDEN.getMessage());
    }
}