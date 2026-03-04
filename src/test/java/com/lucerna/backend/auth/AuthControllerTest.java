package com.lucerna.backend.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucerna.backend.auth.controller.AuthController;
import com.lucerna.backend.auth.dto.RefreshRequest;
import com.lucerna.backend.auth.dto.TokenRequest;
import com.lucerna.backend.auth.dto.TokenResponse;
import com.lucerna.backend.auth.service.PkceTokenService;
import com.lucerna.backend.common.config.SecurityConfig;
import com.lucerna.backend.common.exception.BusinessException;
import com.lucerna.backend.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AuthController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class
        },
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = SecurityConfig.class
        )
)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PkceTokenService pkceTokenService;

    private static final TokenResponse MOCK_TOKEN = new TokenResponse(
            "access-token", "refresh-token", "id-token", "Bearer", 300L
    );

    @Test
    @DisplayName("POST /auth/token - 정상 요청 시 200과 TokenResponse 반환")
    void token_success() throws Exception {
        given(pkceTokenService.exchangeAuthCode(any(TokenRequest.class))).willReturn(MOCK_TOKEN);

        mockMvc.perform(post("/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new TokenRequest("auth-code", "code-verifier", "app://callback")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.access_token").value("access-token"))
                .andExpect(jsonPath("$.data.refresh_token").value("refresh-token"))
                .andExpect(jsonPath("$.data.token_type").value("Bearer"));
    }

    @Test
    @DisplayName("POST /auth/token - Keycloak 교환 실패 시 401 반환")
    void token_keycloakFailure_returns401() throws Exception {
        given(pkceTokenService.exchangeAuthCode(any(TokenRequest.class)))
                .willThrow(new BusinessException(ErrorCode.AUTH_TOKEN_EXCHANGE_FAILED));

        mockMvc.perform(post("/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new TokenRequest("bad-code", "code-verifier", "app://callback")
                        )))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("AUTH_002"));
    }

    @Test
    @DisplayName("POST /auth/token - 필수 필드 누락 시 400 반환")
    void token_missingField_returns400() throws Exception {
        mockMvc.perform(post("/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\": \"\", \"codeVerifier\": \"xyz\", \"redirectUri\": \"app://callback\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_001"));
    }

    @Test
    @DisplayName("POST /auth/refresh - 정상 요청 시 200과 TokenResponse 반환")
    void refresh_success() throws Exception {
        given(pkceTokenService.refreshToken(any(RefreshRequest.class))).willReturn(MOCK_TOKEN);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshRequest("valid-refresh-token"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.access_token").value("access-token"));
    }

    @Test
    @DisplayName("POST /auth/refresh - 유효하지 않은 토큰 시 400 반환")
    void refresh_invalidToken_returns400() throws Exception {
        given(pkceTokenService.refreshToken(any(RefreshRequest.class)))
                .willThrow(new BusinessException(ErrorCode.AUTH_INVALID_REFRESH_TOKEN));

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshRequest("expired-token"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("AUTH_003"));
    }
}
