package com.lucerna.backend.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 설정.
 * - CORS: 프론트엔드(React/Flutter 등)에서의 API 호출 허용
 * - 운영 환경 배포 시 allowedOriginPatterns를 실제 도메인으로 제한 필요
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 개발 환경: 모든 origin 허용 (운영 시 특정 도메인으로 변경)
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                // 쿠키/인증 헤더 포함 요청 허용
                .allowCredentials(true)
                // preflight 요청 캐시 시간 (1시간)
                .maxAge(3600);
    }
}

