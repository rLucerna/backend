package com.lucerna.backend.auth.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucerna.backend.auth.config.KeycloakProperties;
import com.lucerna.backend.common.exception.BusinessException;
import com.lucerna.backend.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Keycloak Admin REST API 클라이언트.
 * lucerna-admin Confidential Client의 Service Account를 이용한 관리 작업을 수행한다.
 *
 * 사용 사례:
 * - 회원 탈퇴 시 Keycloak 사용자 비활성화 (enabled = false)
 *   → 모든 세션 즉시 종료 + 재로그인 차단
 */
@Slf4j
@Service
public class KeycloakAdminService {

    private final KeycloakProperties keycloakProperties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public KeycloakAdminService(KeycloakProperties keycloakProperties,
                                @Qualifier("keycloakRestClient") RestClient restClient,
                                ObjectMapper objectMapper) {
        this.keycloakProperties = keycloakProperties;
        this.objectMapper = objectMapper;
        this.restClient = restClient;
    }

    /**
     * Keycloak 사용자 비활성화 (회원 탈퇴 시 호출).
     * - enabled = false → 로그인 차단
     * - 기존 세션 전체 즉시 종료 → AT/RT 모두 무효화
     *
     * @param keycloakId Keycloak User UUID (JWT sub claim)
     */
    public void disableUser(String keycloakId) {
        String adminToken = obtainAdminToken();
        String userAdminUri = buildUserAdminUri(keycloakId);

        log.debug("Keycloak 사용자 비활성화 요청 → uri={}", userAdminUri);
        try {
            restClient.put()
                    .uri(userAdminUri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + adminToken)
                    .body(Map.of("enabled", false))
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        String body = new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8);
                        log.error("Keycloak 사용자 비활성화 4xx → status={}, body={}", res.getStatusCode(), body);
                        throw new BusinessException(ErrorCode.USER_WITHDRAW_FAILED);
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        String body = new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8);
                        log.error("Keycloak 사용자 비활성화 5xx → status={}, body={}", res.getStatusCode(), body);
                        throw new BusinessException(ErrorCode.USER_WITHDRAW_FAILED);
                    })
                    .toBodilessEntity();

            log.debug("Keycloak 사용자 비활성화 성공 → keycloakId={}", keycloakId);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Keycloak 사용자 비활성화 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.USER_WITHDRAW_FAILED);
        }
    }

    /**
     * lucerna-admin Client Credentials로 Admin Access Token 획득.
     * grant_type=client_credentials 사용 (Service Account).
     */
    private String obtainAdminToken() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "client_credentials");
        formData.add("client_id", keycloakProperties.getAdminClientId());
        formData.add("client_secret", keycloakProperties.getAdminClientSecret());

        log.debug("Admin Token 획득 요청 → client_id={}", keycloakProperties.getAdminClientId());
        try {
            String responseBody = restClient.post()
                    .uri(keycloakProperties.getTokenUri())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        String body = new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8);
                        log.error("Admin Token 획득 실패 → status={}, body={}", res.getStatusCode(), body);
                        throw new BusinessException(ErrorCode.USER_WITHDRAW_FAILED);
                    })
                    .body(String.class);

            AdminTokenResponse tokenResponse = objectMapper.readValue(responseBody, AdminTokenResponse.class);
            log.debug("Admin Token 획득 성공");
            return tokenResponse.accessToken();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Admin Token 획득 중 오류 발생: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.USER_WITHDRAW_FAILED);
        }
    }

    /** {authServerUrl}/admin/realms/{realm}/users/{keycloakId} */
    private String buildUserAdminUri(String keycloakId) {
        return keycloakProperties.getAuthServerUrl()
                + "/admin/realms/" + keycloakProperties.getRealm()
                + "/users/" + keycloakId;
    }

    private record AdminTokenResponse(@JsonProperty("access_token") String accessToken) {}
}
