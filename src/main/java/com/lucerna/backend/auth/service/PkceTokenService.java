package com.lucerna.backend.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucerna.backend.auth.config.KeycloakProperties;
import com.lucerna.backend.auth.dto.RefreshRequest;
import com.lucerna.backend.auth.dto.TokenRequest;
import com.lucerna.backend.auth.dto.TokenResponse;
import com.lucerna.backend.common.exception.BusinessException;
import com.lucerna.backend.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;

/**
 * Keycloak PKCE 토큰 교환 서비스.
 * - Authorization Code → Access/Refresh Token 교환
 * - Refresh Token → 신규 토큰 갱신
 *
 * Keycloak이 응답 Content-Type을 application/octet-stream으로 보내는 경우가 있어
 * 응답을 String으로 받은 뒤 ObjectMapper로 직접 파싱한다.
 */
@Slf4j
@Service
public class PkceTokenService {

    private final KeycloakProperties keycloakProperties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public PkceTokenService(KeycloakProperties keycloakProperties,
                            RestClient.Builder restClientBuilder,
                            ObjectMapper objectMapper) {
        this.keycloakProperties = keycloakProperties;
        this.objectMapper = objectMapper;
        // JDK HttpClient(Java 21 기본)는 Keycloak 응답 파싱 시 문제 발생
        // HttpURLConnection 기반의 SimpleClientHttpRequestFactory 사용
        this.restClient = restClientBuilder
                .requestFactory(new SimpleClientHttpRequestFactory())
                .build();
    }

    /**
     * PKCE Authorization Code를 Keycloak 토큰으로 교환
     * grant_type: authorization_code (lucerna-app Public Client, secret 없음)
     */
    public TokenResponse exchangeAuthCode(TokenRequest request) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", keycloakProperties.getClientId());
        formData.add("code", request.code());
        formData.add("code_verifier", request.codeVerifier());
        formData.add("redirect_uri", request.redirectUri());

        return callTokenEndpoint(formData, ErrorCode.AUTH_TOKEN_EXCHANGE_FAILED);
    }

    /**
     * Refresh Token으로 신규 토큰 발급
     */
    public TokenResponse refreshToken(RefreshRequest request) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "refresh_token");
        formData.add("client_id", keycloakProperties.getClientId());
        formData.add("refresh_token", request.refreshToken());

        return callTokenEndpoint(formData, ErrorCode.AUTH_INVALID_REFRESH_TOKEN);
    }

    private TokenResponse callTokenEndpoint(MultiValueMap<String, String> formData, ErrorCode clientErrorCode) {
        log.debug("Keycloak 토큰 요청 → uri={}", keycloakProperties.getTokenUri());
        try {
            // Keycloak 응답 Content-Type이 application/octet-stream일 수 있으므로
            // String으로 받아서 ObjectMapper로 직접 파싱
            String responseBody = restClient.post()
                    .uri(keycloakProperties.getTokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        String body = new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8);
                        log.warn("Keycloak 4xx 오류 → status={}, body={}", res.getStatusCode(), body);
                        throw new BusinessException(clientErrorCode);
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        String body = new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8);
                        log.error("Keycloak 5xx 오류 → status={}, body={}", res.getStatusCode(), body);
                        throw new BusinessException(ErrorCode.AUTH_TOKEN_EXCHANGE_FAILED);
                    })
                    .body(String.class);

            return objectMapper.readValue(responseBody, TokenResponse.class);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Keycloak 토큰 교환 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.AUTH_TOKEN_EXCHANGE_FAILED);
        }
    }
}
