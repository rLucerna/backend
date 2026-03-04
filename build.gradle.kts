plugins {
    java
    id("org.springframework.boot") version "3.5.11"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.lucerna"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

// Lombok annotationProcessor가 compileOnly 스코프에서도 동작하도록 설정
configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // === Spring Boot Starters ===
    implementation("org.springframework.boot:spring-boot-starter-web")          // REST API (내장 Tomcat)
    implementation("org.springframework.boot:spring-boot-starter-security")     // Spring Security
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server") // JWT 검증 (Keycloak)
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")    // JPA/Hibernate
    implementation("org.springframework.boot:spring-boot-starter-validation")  // Bean Validation (@Valid)
    implementation("org.springframework.boot:spring-boot-starter-actuator")    // 헬스체크/모니터링

    // === Database ===
    runtimeOnly("org.postgresql:postgresql")  // PostgreSQL JDBC 드라이버

    // === QueryDSL (타입 안전 동적 쿼리) ===
    implementation("com.querydsl:querydsl-jpa:5.1.0:jakarta")
    annotationProcessor("com.querydsl:querydsl-apt:5.1.0:jakarta")
    annotationProcessor("jakarta.annotation:jakarta.annotation-api")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")

    // === Lombok (보일러플레이트 코드 제거) ===
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // === Test ===
    testImplementation("org.springframework.boot:spring-boot-starter-test")  // JUnit5 + MockMvc + AssertJ
    testImplementation("org.springframework.security:spring-security-test")  // @WithMockUser 등
    testRuntimeOnly("com.h2database:h2")                                     // 테스트용 인메모리 DB
    testImplementation("org.wiremock:wiremock-standalone:3.4.2")              // 외부 API 모킹 (Keycloak 등)
}

tasks.withType<Test> {
    useJUnitPlatform()
}