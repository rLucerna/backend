---

날짜: 2026-03-07
작업 이름: JWT 토큰 검증 + 만료 대응 구현
수정 대상: ErrorCode.java, SecurityConfig.java
신규 파일: JwtAuthenticationEntryPoint.java, JwtAccessDeniedHandler.java, PingController.java, 테스트 3종
결과: 토큰 만료/무효 구분 401, 권한 없음 403 - 전체 테스트 통과

---

## 구현 내용

### ErrorCode 추가
- `AUTH_005`: 만료된 토큰 (클라이언트 → /auth/refresh 호출)
- `AUTH_006`: 유효하지 않은 토큰 (재로그인 필요)
- `AUTH_007`: 권한 없음 (재로그인으로도 해결 불가)

### JwtAuthenticationEntryPoint
`InvalidBearerTokenException → JwtValidationException → OAuth2Error` 체인에서 "expired" 포함 여부로 만료/무효 구분.

### SecurityConfig
`exceptionHandling().authenticationEntryPoint()` + `oauth2ResourceServer().authenticationEntryPoint()` 양쪽에 등록.
(`exceptionHandling`: 토큰 없음 등 일반 인증 실패 / `oauth2ResourceServer`: JWT 검증 실패)

### PingController 테스트 패턴
`standaloneSetup` + `MockHttpServletRequest.setUserPrincipal(auth)` 방식.
- `springSecurity()` 는 실제 `springSecurityFilterChain` 빈 필요 → standaloneSetup 불가
- `@WithMockUser` 는 `@WebMvcTest` + excluded security 환경에서 Authentication 파라미터 null
- `setUserPrincipal()`: Spring MVC `ServletRequestMethodArgumentResolver`가 `Principal.isAssignableFrom(Authentication)` 조건으로 직접 주입

---

---

날짜: 2026-03-07
작업 이름: token-dev 엔드포인트 응답 형식 수정
수정 대상: `AuthController.java`
결과: Postman OAuth2 클라이언트가 access_token 정상 추출

---

## 원인 분석

`POST /auth/token-dev`가 `CommonResponse<TokenResponse>`로 응답하여
Postman OAuth2 클라이언트가 루트 레벨에서 `access_token`을 찾지 못함.

OAuth2 표준(RFC 6749)에 따르면 토큰 엔드포인트 응답은 `access_token`이 루트에 위치해야 함.

## 수정 내용

| 항목 | 변경 전 | 변경 후 |
|------|---------|---------|
| `tokenDev()` 반환 타입 | `ResponseEntity<CommonResponse<TokenResponse>>` | `ResponseEntity<TokenResponse>` |
| 응답 구조 | `{"success":true,"data":{"access_token":"..."}}` | `{"access_token":"...","refresh_token":"..."}` |

> `/auth/token` 및 `/auth/refresh`는 일반 앱 API이므로 `CommonResponse` 유지.

---

---

날짜: 2026-03-04
작업 이름: 공통 개발 환경 설정 보완 + PKCE 토큰 교환 버그 수정
수정 대상: PkceTokenService.java, SecurityConfig.java, WebConfig.java, GlobalExceptionHandler.java, ErrorCode.java, BusinessException.java, CommonResponse.java, build.gradle.kts, application.yml, application-test.yml
신규 파일: JpaConfig.java, BaseEntity.java
결과: PKCE 토큰 교환 성공 (Authorization Code + Refresh Token), 전체 테스트 통과

---

## 1. 공통 개발 환경 설정 보완

### [1] 기존 파일 주석 추가 (7개 파일)

| 파일 | 추가된 주석 |
|------|-------------|
| `SecurityConfig.java` | 클래스 설명, CSRF/세션/엔드포인트 설정 인라인 주석 |
| `WebConfig.java` | CORS 설정 목적, 운영 환경 주의사항 |
| `GlobalExceptionHandler.java` | 클래스 역할, 각 핸들러 메서드 한줄 설명 |
| `ErrorCode.java` | enum 사용 형식 안내, 도메인별 접두사 규칙 |
| `BusinessException.java` | 사용 예시 포함 |
| `CommonResponse.java` | 성공/실패 JSON 형태 예시, 팩토리 메서드 설명 |
| `build.gradle.kts` | 모든 의존성에 한줄 역할 설명 |
| `application.yml` | 섹션별 구분 주석 |
| `application-test.yml` | 테스트 프로필 활성화 방법 안내 |

### [2] JPA Auditing 설정 + BaseEntity 생성

- `common/config/JpaConfig.java` — `@EnableJpaAuditing` 활성화
- `common/entity/BaseEntity.java` — `@CreatedDate`, `@LastModifiedDate` 자동 관리

---

## 2. PKCE 토큰 교환 버그 수정 (PkceTokenService.java)

### 원인 분석

Spring Boot → Keycloak 토큰 교환 시 3가지 문제가 겹쳐 발생.

#### 문제 1: Jackson Converter가 Form 데이터를 가로챔

- Jackson이 `MultiValueMap`을 JSON으로 직렬화 → Keycloak: `Missing form parameter: grant_type`
- 해결: converter 조작 제거, 기본 목록 유지 (FormHttpMessageConverter가 먼저 처리)

#### 문제 2: Keycloak 응답 Content-Type이 `application/octet-stream`

- Spring RestClient가 적합한 converter를 찾지 못해 파싱 실패
- 해결: `.body(String.class)`로 받아 `objectMapper.readValue()`로 직접 파싱

#### 문제 3: JDK 21 HttpClient 호환성 문제

- `JdkClientHttpRequestFactory`에서 Keycloak 응답 읽기 실패
- 해결: `SimpleClientHttpRequestFactory` (HttpURLConnection 기반) 명시적 사용

### 수정 내용

| 항목 | 변경 전 | 변경 후 |
|------|---------|---------|
| Converter 설정 | Jackson을 인덱스 0에 추가 | converter 조작 제거 |
| 응답 파싱 | `.body(TokenResponse.class)` | `.body(String.class)` + `objectMapper.readValue()` |
| RequestFactory | JDK 기본 | `SimpleClientHttpRequestFactory` |

---

## 검증

| 테스트 | 결과 |
|--------|------|
| Spring Boot /auth/token → Keycloak | 성공 |
| Spring Boot /auth/refresh → Keycloak | 성공 |
| `./gradlew test` (전체) | 통과 |

---

---

날짜: 2026-02-20
작업 이름: Keycloak Helm 설치 오류 수정 및 브라우저 접속 설정
수정 대상: keycloak/values.yaml, hosts 파일
결과: http://lucerna/auth/ 브라우저 접속 성공

---

## 원인 분석

1. **Chart-Image 버전 불일치**: `codecentric/keycloak:18.10.0` (Wildfly) vs Keycloak 26 (Quarkus) → CrashLoopBackOff
2. **StatefulSet command/args 미반영**: `kc.sh`가 인자 없이 실행 → exit 0 → CrashLoop
3. **브라우저 무한 로딩**: `KC_HOSTNAME=lucerna` + hosts에 minikube 내부 IP(`192.168.49.2`) → Windows에서 접근 불가

## 수정 내용

| 항목 | 변경 전 | 변경 후 |
|------|---------|---------|
| 차트 | `codecentric/keycloak:18.10.0` | `codecentric/keycloakx:7.1.8` |
| command/args | command: `["start-dev"]` | command: `[]`, args: `["start-dev"]` |
| cache.stack | 없음 | `tcp` (jdbc-ping 미지원 대응) |
| proxy.mode | 없음 | `xforwarded` |
| hosts 파일 | `192.168.49.2 lucerna` | `127.0.0.1 lucerna` |
| minikube tunnel | 미사용 | 실행 (`minikube tunnel &`) |

## 발생했던 오류 요약

| 오류 | 해결 |
|------|------|
| `CrashLoopBackOff` (exit 0) | keycloakx 차트로 마이그레이션 |
| `duplicate entries [KC_HTTP_RELATIVE_PATH]` | extraEnv에서 제거 |
| `ISPN000540: No such JGroups stack 'jdbc-ping'` | `cache.stack: tcp` 설정 |
| `192.168.49.2` 접속 불가 | minikube tunnel + hosts `127.0.0.1` 변경 |
