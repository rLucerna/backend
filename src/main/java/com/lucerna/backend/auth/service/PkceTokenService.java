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
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Service
public class PkceTokenService {

    private final KeycloakProperties keycloakProperties;
    private final RestClient restClient;

    public PkceTokenService(KeycloakProperties keycloakProperties,
                            RestClient.Builder restClientBuilder,
                            ObjectMapper objectMapper) {
        this.keycloakProperties = keycloakProperties;

        // Keycloak이 200 성공 응답에 Content-Type 헤더를 생략하는 경우
        // Spring이 이를 application/octet-stream으로 기본 처리하여 변환 실패함.
        // MappingJackson2HttpMessageConverter의 지원 타입에 application/octet-stream과 */*를 추가하여
        // Content-Type에 무관하게 JSON 파싱이 가능하도록 설정.
        MappingJackson2HttpMessageConverter jacksonConverter =
                new MappingJackson2HttpMessageConverter(objectMapper);
        jacksonConverter.setSupportedMediaTypes(List.of(
                MediaType.APPLICATION_JSON,
                new MediaType("application", "*+json"),
                MediaType.APPLICATION_OCTET_STREAM,
                MediaType.ALL
        ));

        this.restClient = restClientBuilder
                .requestFactory(new SimpleClientHttpRequestFactory())
                .messageConverters(converters -> {
                    converters.removeIf(c -> c instanceof MappingJackson2HttpMessageConverter);
                    converters.add(0, jacksonConverter);
                })
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
            return restClient.post()
                    .uri(keycloakProperties.getTokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON)
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
                    .body(TokenResponse.class);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Keycloak 토큰 교환 중 오류 발생: {}", e.getMessage());
            throw new BusinessException(ErrorCode.AUTH_TOKEN_EXCHANGE_FAILED);
        }
    }
}
