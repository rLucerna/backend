# 프로젝트 아키텍처

## 패키지 구조 (현재)

```
com.lucerna.backend
├── BackendApplication.java
├── auth/                              # 인증 도메인
│   ├── config/
│   │   └── KeycloakProperties.java   # @ConfigurationProperties("keycloak")
│   ├── controller/
│   │   └── AuthController.java       # POST /auth/token, /auth/refresh, /auth/token-dev
│   ├── dto/
│   │   ├── TokenRequest.java         # code, codeVerifier, redirectUri
│   │   ├── RefreshRequest.java       # refreshToken
│   │   └── TokenResponse.java        # access/refresh/id token + @JsonNaming(SnakeCase)
│   └── service/
│       └── PkceTokenService.java     # RestClient 기반 Keycloak 토큰 교환
└── common/                           # 공통 모듈
    ├── config/
    │   ├── JpaConfig.java            # @EnableJpaAuditing
    │   ├── SecurityConfig.java       # JWT 검증, /auth/** permitAll
    │   └── WebConfig.java            # CORS 설정
    ├── entity/
    │   └── BaseEntity.java           # @CreatedDate, @LastModifiedDate (모든 엔티티 상속)
    ├── exception/
    │   ├── BusinessException.java    # ErrorCode 기반 커스텀 예외
    │   ├── ErrorCode.java            # 도메인별 접두사 Enum (AUTH_*, ...)
    │   └── GlobalExceptionHandler.java
    └── response/
        └── CommonResponse<T>         # { success, data, error } 공통 응답 래퍼
```

---

## 도메인 계획 (예정)

| 도메인 | 패키지 | 기능 |
|--------|--------|------|
| 사용자 | `user/` | 사용자 관리, 프로필 |
| 자선활동 | `charity/` | 봉사/기부 검색, 활동 관리 |
| 수첩 | `note/` | 글쓰기 CRUD |
| 오두막 | `cabin/` | 가상 커뮤니티 공간 |
| 등불 | `lantern/` | 기름/성냥 아이템, 등불 점등 |

---

## 핵심 설계 원칙

- **계층**: Controller → Service → Repository (레이어드 아키텍처)
- **응답**: 모든 API는 `CommonResponse<T>` 래퍼 사용
  - 예외: OAuth2 표준 엔드포인트(`/auth/token-dev`)는 토큰 직접 반환
- **예외**: `GlobalExceptionHandler` 집중 처리, `BusinessException` + `ErrorCode` 조합
- **엔티티**: 모든 엔티티는 `BaseEntity` 상속 (createdAt, updatedAt 자동 관리)
- **인증**: Keycloak JWT 검증, Spring Security OAuth2 Resource Server

---

## 설정값 (개발 환경)

| 항목 | 값 |
|------|-----|
| Spring Boot Port | 8081 |
| Keycloak URL | `http://lucerna/auth` |
| Realm | `lucerna_test` |
| Client ID (Public) | `lucerna-app` |
| PKCE Method | S256 |
| JWT Issuer URI | `http://lucerna/auth/realms/lucerna_test` |
| PostgreSQL | `localhost:5432/lucerna_db` (port-forward: 65432) |

---

## 테스트 패턴

| 테스트 종류 | 방식 |
|------------|------|
| Controller | `@WebMvcTest` + `excludeAutoConfiguration = {SecurityAutoConfiguration, OAuth2ResourceServerAutoConfiguration}` + `excludeFilters = SecurityConfig` |
| GlobalExceptionHandler | `MockMvcBuilders.standaloneSetup()` (@WebMvcTest inner class 라우트 미등록 문제) |
| Service (외부 HTTP) | `@WireMockTest` (Spring 컨텍스트 없이 RestClient + WireMock) |
