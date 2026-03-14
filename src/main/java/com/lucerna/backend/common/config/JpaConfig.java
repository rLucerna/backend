package com.lucerna.backend.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing 활성화.
 * BaseEntity의 @CreatedDate, @LastModifiedDate가 자동으로 값을 채우려면 필요.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}