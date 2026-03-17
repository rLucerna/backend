package com.lucerna.backend.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Keycloak 통신용 RestClient 설정.
 * JDK 21 기본 HttpClient는 Keycloak 응답 파싱 문제가 있어
 * HttpURLConnection 기반의 SimpleClientHttpRequestFactory를 사용한다.
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient keycloakRestClient(RestClient.Builder builder) {
        return builder
                .requestFactory(new SimpleClientHttpRequestFactory())
                .build();
    }
}
