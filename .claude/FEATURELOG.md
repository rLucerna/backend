# backend && infra 기능 작업 현황

---

## [FEAT-001] PKCE 기반 사용자 인증

**기능 이름**: Authorization Code Flow + PKCE 인증
**담당 서비스**: Keycloak (인증 서버) + Spring Boot (Token Exchange Proxy, Option C)
**인증 방식**: OAuth2 Authorization Code Flow + PKCE (S256)
**개발 환경 클라이언트**: Postman (Public Client, 앱 대체)
**실서비스 클라이언트**: React Native 앱 (deep link callback)

**현황**: `완료`

---

### 작업 로그

| 단계 | 내용 | 상태 |
|------|------|------|
| Keycloak 인프라 구성 | minikube Helm 배포 (`codecentric/keycloakx:7.1.8`) | ✅ 완료 |
| Keycloak Realm 생성 | `lucerna_test` realm | ✅ 완료 |
| Keycloak Client 구성 | `lucerna-app` (Public, PKCE S256 강제) | ✅ 완료 |
| Keycloak Client 구성 | `lucerna-admin` (Confidential, Service Account) | ✅ 완료 |
| Postman PKCE 인증 테스트 | Access / Refresh / ID Token 수신 확인 | ✅ 완료 |
| Spring Boot 프로젝트 세팅 | Java 21, Spring Boot 3.5.11, Gradle Kotlin DSL | ✅ 완료 |
| Spring Boot 의존성 구성 | Web, Security, OAuth2 Resource Server, JPA, Actuator 등 | ✅ 완료 |
| Spring Boot 인증 구현 | PKCE Token Exchange 엔드포인트 (`POST /auth/token`, `/auth/refresh`) | ✅ 완료 |
| Spring Boot API 보호 | JWT 검증 필터, SecurityConfig | ✅ 완료 |
| Spring Boot ↔ Keycloak 통합 테스트 | Postman으로 전체 흐름 검증 (curl 포함) | ✅ 완료 |
| Postman OAuth2 클라이언트 호환 수정 | `/auth/token-dev` → `TokenResponse` 직접 반환 (CommonResponse 제거) | ✅ 완료 |
| minikube Pod 배포 | Spring Boot Helm/Manifest 작성 및 배포 | ⬜ 미시작 |

---

### 구현 흐름 (Option C)

```
앱 → Keycloak (PKCE 인증) → 앱 (auth_code 수신)
앱 → POST /auth/token (auth_code + code_verifier)
Spring Boot → Keycloak 토큰 교환
Spring Boot → 앱 (access_token, refresh_token, id_token 반환)
보호된 API → Spring Boot JWT 검증 (Keycloak JWKS)
```

---

### API 엔드포인트

| 메서드 | 경로 | 응답 형식 | 설명 |
|--------|------|-----------|------|
| POST | `/auth/token` | `CommonResponse<TokenResponse>` | PKCE code → Keycloak 토큰 교환 |
| POST | `/auth/refresh` | `CommonResponse<TokenResponse>` | Refresh Token 갱신 |
| POST | `/auth/token-dev` | `TokenResponse` (직접) | Postman OAuth2 클라이언트 전용 (DEV only) |

> `/auth/token-dev`는 OAuth2 표준(RFC 6749)에 따라 `access_token`을 루트에 반환해야 하므로 `CommonResponse` 미사용.

---

---

## [FEAT-002] JWT 토큰 검증 + 만료 대응

**현황**: `완료`

### 구현 내용

| 파일 | 역할 |
|------|------|
| `ErrorCode` | `AUTH_TOKEN_EXPIRED(AUTH_005)`, `AUTH_TOKEN_INVALID(AUTH_006)`, `AUTH_FORBIDDEN(AUTH_007)` 추가 |
| `auth/handler/JwtAuthenticationEntryPoint` | JWT 검증 실패 시 만료/무효 구분 → 401 CommonResponse 반환 |
| `auth/handler/JwtAccessDeniedHandler` | 권한 부족 → 403 CommonResponse 반환 |
| `SecurityConfig` | 위 두 핸들러 등록 |
| `common/controller/PingController` | 인증 테스트용 `GET /api/v1/ping` (DEV only) |

### 클라이언트 처리 흐름

```
API 호출
  ├─ 200 OK               → 정상
  ├─ 401 AUTH_005 (만료)  → POST /auth/refresh → 새 토큰 → API 재호출
  ├─ 401 AUTH_006 (무효)  → 재로그인 필요
  └─ 403 AUTH_007 (권한)  → 재로그인으로도 해결 불가
```

### 테스트 (전체 통과)

| 테스트 | 케이스 |
|--------|--------|
| `PingControllerTest` | 인증된 사용자 200 |
| `JwtAuthenticationEntryPointTest` | 만료→AUTH_005 / 서명 무효→AUTH_006 / 기타→AUTH_006 |
| `JwtAccessDeniedHandlerTest` | 권한 없음→AUTH_007 |

---

## [FEAT-003] 사용자 정보 조회 (`GET /api/v1/users/me`)

**기능 이름**: 사용자 정보 JIT 조회/생성
**현황**: `완료`

---

### 작업 로그

| 단계 | 내용 | 상태 |
|------|------|------|
| User Entity 설계 | `User` (extends BaseEntity), `UserStatus` enum, `Lodge` | ✅ 완료 |
| Repository 생성 | `UserRepository.findByKeycloakId()`, `LodgeRepository.findByUserId()` | ✅ 완료 |
| ErrorCode 추가 | `USER_NOT_FOUND(USER_001)`, `USER_EMAIL_NOT_FOUND(USER_002)` | ✅ 완료 |
| DTO 생성 | `UserMeResponse`, `LodgeSummary` record | ✅ 완료 |
| Service 구현 | `UserService.getOrCreateUser()` — JIT 생성 + lastLoginAt 갱신 | ✅ 완료 |
| Controller 구현 | `GET /api/v1/users/me` — `@AuthenticationPrincipal Jwt` | ✅ 완료 |
| 단위 테스트 | `UserServiceTest` (7개), `UserControllerTest` (2개) | ✅ 완료 |

### API 엔드포인트

| 메서드 | 경로 | 응답 형식 | 설명 |
|--------|------|-----------|------|
| GET | `/api/v1/users/me` | `CommonResponse<UserMeResponse>` | 현재 사용자 조회 (없으면 JIT 생성) |

### JIT 생성 흐름

```
getOrCreateUser(keycloakId, email)
├─ findByKeycloakId(keycloakId)
│  ├─ 존재 → lastLoginAt 갱신 + Lodge 조회 → UserMeResponse(isNewUser=false)
│  └─ 미존재 → User.createNewUser() → save → UserMeResponse(isNewUser=true, lodge=null)
```

---

## [FEAT-004] 소셜 인증 (Google / Apple / Kakao)

**현황**: `계획 중` → `Plans.md` 참고

---
