package com.lucerna.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration // "스프링아, 서버 켜질 때 이 설정 파일부터 읽어줘!"
@EnableWebSecurity // "스프링 시큐리티(경호원) 설정 내가 직접 할게!"
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF 비활성화 (로컬 테스트에서 POST 요청을 쏘기 위한 필수 설정)
                .csrf(AbstractHttpConfigurer::disable)

                // 2. 요청 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 🔥 우리가 만든 수첩 API 주소는 토큰(신분증) 없이도 무조건 통과!
                        // TODO: 프론트 연동 전까지 임시로 권한 오픈
                        .requestMatchers("/api/v1/notebooks/**").permitAll()

                        // 그 외의 다른 모든 요청은 로그인해야 접근 가능
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}