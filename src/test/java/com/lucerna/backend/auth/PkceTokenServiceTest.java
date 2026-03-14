package com.lucerna.backend.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucerna.backend.auth.config.KeycloakProperties;
import com.lucerna.backend.auth.dto.RefreshRequest;
import com.lucerna.backend.auth.dto.TokenRequest;
import com.lucerna.backend.auth.dto.TokenResponse;
import com.lucerna.backend.auth.service.PkceTokenService;
import com.lucerna.backend.common.exception.BusinessException;
import com.lucerna.backend.common.exception.ErrorCode;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@WireMockTest
class PkceTokenServiceTest {

    private static final String TOKEN_PATH = "/realms/lucerna_test/protocol/openid-connect/token";

    private PkceTokenService service;

    @BeforeEach
    void setUp(WireMockRuntimeInfo wmRuntimeInfo) {
        KeycloakProperties props = new KeycloakProperties();
        props.setAuthServerUrl(wmRuntimeInfo.getHttpBaseUrl());
        props.setRealm("lucerna_test");
        props.setClientId("lucerna-app");
        props.setTokenUri(wmRuntimeInfo.getHttpBaseUrl() + TOKEN_PATH);

        service = new PkceTokenService(props, RestClient.builder(), new ObjectMapper());
    }

    @Test
    @DisplayName("auth_code 교환 성공 시 TokenResponse 반환")
    void exchangeAuthCode_success(WireMockRuntimeInfo wmRuntimeInfo) {
        stubFor(post(urlEqualTo(TOKEN_PATH))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "access_token": "test-access-token",
                                  "refresh_token": "test-refresh-token",
                                  "id_token": "test-id-token",
                                  "token_type": "Bearer",
                                  "expires_in": 300
                                }
                                """)));

        TokenRequest request = new TokenRequest("auth-code", "code-verifier-xyz", "app://lucerna/callback");
        TokenResponse response = service.exchangeAuthCode(request);

        assertThat(response.accessToken()).isEqualTo("test-access-token");
        assertThat(response.refreshToken()).isEqualTo("test-refresh-token");
        assertThat(response.idToken()).isEqualTo("test-id-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(300L);
    }

    @Test
    @DisplayName("Keycloak 400 응답 시 AUTH_TOKEN_EXCHANGE_FAILED 예외 발생")
    void exchangeAuthCode_keycloak400_throwsException() {
        stubFor(post(urlEqualTo(TOKEN_PATH))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\": \"invalid_grant\"}")));

        assertThatThrownBy(() ->
                service.exchangeAuthCode(new TokenRequest("bad-code", "verifier", "app://callback")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.AUTH_TOKEN_EXCHANGE_FAILED));
    }

    @Test
    @DisplayName("refresh_token 갱신 성공 시 새로운 TokenResponse 반환")
    void refreshToken_success() {
        stubFor(post(urlEqualTo(TOKEN_PATH))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "access_token": "new-access-token",
                                  "refresh_token": "new-refresh-token",
                                  "id_token": "new-id-token",
                                  "token_type": "Bearer",
                                  "expires_in": 300
                                }
                                """)));

        TokenResponse response = service.refreshToken(new RefreshRequest("old-refresh-token"));

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
    }

    @Test
    @DisplayName("만료된 refresh_token 시 AUTH_INVALID_REFRESH_TOKEN 예외 발생")
    void refreshToken_expired_throwsException() {
        stubFor(post(urlEqualTo(TOKEN_PATH))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\": \"invalid_grant\", \"error_description\": \"Token is not active\"}")));

        assertThatThrownBy(() ->
                service.refreshToken(new RefreshRequest("expired-token")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                        .isEqualTo(ErrorCode.AUTH_INVALID_REFRESH_TOKEN));
    }
}
