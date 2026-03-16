package com.lucerna.backend.common.config;

import com.lucerna.backend.auth.filter.DevMockJwtFilter;
import com.lucerna.backend.auth.handler.JwtAccessDeniedHandler;
import com.lucerna.backend.auth.handler.JwtAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Spring Security 설정.
 * - JWT 기반 Stateless 인증 (Keycloak OAuth2 Resource Server)
 * - CSRF 비활성화 (REST API 서버이므로 불필요)
 * - 공개 엔드포인트: /auth/**, /actuator/health
 * - 인증 실패: JwtAuthenticationEntryPoint (만료/무효 구분 401)
 * - 권한 없음: JwtAccessDeniedHandler (403)
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    /** dev 프로필에서만 주입. prod에서는 Bean 미존재 → Mock 필터 미등록 */
    private final ObjectProvider<DevMockJwtFilter> devMockJwtFilterProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // dev 환경: Mock JWT 필터를 인증 필터 앞에 배치
        devMockJwtFilterProvider.ifAvailable(filter ->
                http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class));

        return http
                // WebConfig의 CORS 설정을 Spring Security 필터에 위임
                .cors(Customizer.withDefaults())
                // REST API 서버 → CSRF 보호 불필요
                .csrf(AbstractHttpConfigurer::disable)
                // JWT 인증 → 서버 측 세션 미사용
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 엔드포인트별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/actuator/health").permitAll()
                        .anyRequest().authenticated())
                // 인증/권한 실패 시 CommonResponse 형식으로 응답
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler))
                // Keycloak JWTjsw 검증 및 역할 변환
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(keycloakJwtAuthenticationConverter()))
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .build();
    }

    /**
     * Keycloak JWT의 realm_access.roles를 Spring Security의 ROLE_* GrantedAuthority로 변환.
     * 예: Keycloak roles=["user","admin"] → ROLE_user, ROLE_admin
     */
    @Bean
    public JwtAuthenticationConverter keycloakJwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Map<String,
                    Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess == null || !realmAccess.containsKey("roles")) {
                return List.of();
            }
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) realmAccess.get("roles");
            return roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());
        });
        return converter;
    }
}
