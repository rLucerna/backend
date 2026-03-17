package com.lucerna.backend.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * 개발 환경 전용 Mock JWT 인증 필터.
 * dev 프로필에서만 활성화되며, Authorization 헤더가 없는 요청에 Mock 사용자를 자동 주입한다.
 *
 * 동작 방식:
 * - Authorization 헤더 있음 → pass through (실제 Keycloak JWT 인증)
 * - Authorization 헤더 없음 → Mock Jwt 생성 후 SecurityContext에 주입
 *
 * Mock 사용자: keycloakId="dev-mock-keycloak-id", email="dev@lucerna.test", role=user
 * 서비스 DB에 해당 사용자가 존재해야 함 → data-dev.sql로 초기 데이터 삽입
 */
@Slf4j
@Component
@Profile("dev")
public class DevMockJwtFilter extends OncePerRequestFilter {

    private static final String MOCK_KEYCLOAK_ID = "dev-mock-keycloak-id";
    private static final String MOCK_EMAIL = "dev@lucerna.test";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Authorization 헤더가 있으면 실제 JWT 인증 흐름 사용
        if (request.getHeader("Authorization") != null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Mock Jwt 생성 및 SecurityContext 주입
        Jwt mockJwt = Jwt.withTokenValue("mock-dev-token")
                .header("alg", "none")
                .claim("sub", MOCK_KEYCLOAK_ID)
                .claim("email", MOCK_EMAIL)
                .claim("realm_access", Map.of("roles", List.of("user")))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        JwtAuthenticationToken authentication = new JwtAuthenticationToken(
                mockJwt, List.of(new SimpleGrantedAuthority("ROLE_user")));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.debug("[DEV] Mock 인증 주입 → {} {}, user={}", request.getMethod(), request.getRequestURI(), MOCK_KEYCLOAK_ID);
        filterChain.doFilter(request, response);
    }
}
