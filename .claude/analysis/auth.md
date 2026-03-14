# 인증 시스템 분석 — 로그아웃 설계 근거

## 현재 인증 아키텍처

### 토큰 흐름

```
앱 → Keycloak (PKCE 인증) → auth_code 수신
앱 → POST /auth/token (auth_code + code_verifier) → Spring Boot → Keycloak → TokenResponse
앱: Access Token + Refresh Token 로컬 저장
앱 → GET /api/v1/** (Authorization: Bearer <AT>) → Spring Boot JWT 검증
AT 만료 → POST /auth/refresh (RT) → 새 AT 발급
```

### 핵심 특성

| 항목 | 현재 설정 |
|------|----------|
| 인증 방식 | JWT Stateless (Keycloak OAuth2 Resource Server) |
| 세션 정책 | `SessionCreationPolicy.STATELESS` — 서버 세션 없음 |
| 토큰 저장 | 클라이언트(앱)에서 관리 |
| Refresh Token | Keycloak이 관리, 앱에서 보관 |
| AT 만료 시간 | Keycloak Realm 설정 (기본 5분) |
| RT 만료 시간 | Keycloak Realm 설정 (기본 30분) |
| 공개 엔드포인트 | `/auth/**`, `/actuator/health` |

---

## JWT Stateless 로그아웃의 본질적 한계

### Access Token은 서버에서 즉시 무효화할 수 없다

JWT는 서명 기반 자기 완결형(self-contained) 토큰이다.
서버가 토큰을 "취소"하려면 모든 요청마다 블랙리스트를 조회해야 하며, 이는 Stateless의 장점을 포기하는 것이다.

### 로그아웃의 실질적 의미

```
시간축 ──────────────────────────────────────►

[로그인]                    [로그아웃]         [AT 만료]
   │                           │                 │
   │◄── Access Token 유효 ────►│◄── 여전히 유효 ►│
   │                           │                 │
                               │  이 구간: AT 만료까지 │
                               │  (최대 5분)         │
```

- AT는 만료 전까지 기술적으로 유효하지만, 앱이 토큰을 삭제하면 실질적 호출 불가
- RT를 Keycloak에서 무효화하면 새 AT 발급이 차단됨
- **결론**: RT 무효화 + 앱 토큰 삭제 = 실질적 로그아웃 완료

---

## Keycloak 로그아웃 실패 시 세션 잔존 문제

### 문제

API 오류로 Keycloak에 RT revoke 요청이 실패하면 Keycloak 세션이 남는다.

### 결론: 별도 대응 불필요

- Keycloak 세션은 `SSO Session Idle` / `SSO Session Max` 설정에 의해 자동 만료
- 앱에서 로컬 토큰 삭제만으로 사용자 입장의 로그아웃은 완료
- 세션 자동 정리는 Keycloak 인프라의 책임
- 대규모 서비스가 아니면 좀비 세션 축적은 실질적 문제 아님

---

## Access Token revoke 미수행 시 보안 문제

### 위험 시나리오

악의적 사용자가 로그아웃 전 AT를 복사 → 로그아웃 후 AT 만료 전까지 API 호출 가능

### 대응 전략별 비교

| 방식 | 즉시 무효화 | 서버 부하 | 복잡도 | 적합 시점 |
|------|-----------|----------|--------|----------|
| AT 짧은 만료 (5분) | ❌ (5분 유예) | 없음 | 낮음 | **현재 프로젝트** |
| JWT + Redis Blacklist | ✅ | 중간 | 높음 | 금융/의료 서비스 |
| Stateful Session | ✅ | 높음 | 중간 | Stateless 포기 |

### 채택: AT 짧은 만료 + 앱 토큰 삭제

- 앱에서 로그아웃 시 토큰 삭제 → Authorization 헤더 구성 불가 → API 호출 자체 불가
- AT 만료 5분은 일반 서비스에서 허용 가능한 수준
- Redis Blacklist는 현 단계에서 오버 엔지니어링

---

## 로그아웃 설계 결정

### Keycloak 엔드포인트 선택 과정

| 시도 | 엔드포인트 | 결과 | 원인 |
|------|-----------|------|------|
| 1차 | `/logout` + `refresh_token` | 실패 (세션 잔존) | Keycloak 18+: `id_token_hint` 없으면 세션 종료 안 함 |
| 2차 | `/revoke` + `token` | 실패 (토큰 유효) | Public Client: `client_secret` 없어 신원 검증 불가 → 형식적 200만 반환 |
| **3차 (채택)** | `/logout` + `id_token_hint` | **성공** | OIDC RP-Initiated Logout 표준 방식 |

### OIDC RP-Initiated Logout 동작 원리

```
POST /logout?id_token_hint=<id_token>
    ↓
Keycloak: id_token 서명 검증 (JWT 자기 완결형)
    ↓
id_token의 "sid" claim에서 세션 ID 추출
    ↓
서버 측 세션 조회 및 삭제
    ↓
이후 해당 세션의 RT로 /refresh 호출 시 "세션 없음" → 거부
```

세션이 서버의 원천 정보(source of truth)이므로, 세션 삭제 = 해당 세션 발급 토큰 전체 무효화.

### Public Client vs Confidential Client revoke 차이

| Client 유형 | `/revoke` 동작 |
|-------------|---------------|
| Confidential | `client_secret`으로 신원 검증 → 실제 revoke |
| **Public (lucerna-app)** | 신원 검증 불가 → 200 반환하지만 실제 무효화 없음 |

### 최종 구현 방식 (2단계)

```
1단계 (필수): POST /logout + id_token_hint → 세션 삭제
2단계 (보험): POST /revoke + refresh_token → 실패해도 1단계로 이미 무효화됨
```

### 구현 파일

| 파일 | 유형 | 변경 |
|------|------|------|
| `auth/dto/LogoutRequest.java` | 신규 | `idToken`, `refreshToken` 필드 |
| `PkceTokenService.java` | 수정 | `logout()` → `terminateSession()` + `tryRevokeRefreshToken()` |
| `KeycloakProperties.java` | 수정 | `logoutUri`, `revokeUri` 필드 |
| `AuthController.java` | 수정 | `POST /auth/logout` |
| `application-dev.yml` | 수정 | `logout-uri`, `revoke-uri` 설정 |
| `ErrorCode.java` | 수정 | `AUTH_LOGOUT_FAILED(AUTH_008)` 추가 |

맞습니다. 이론적으로 **충분히 가능한 공격 벡터**입니다. 이를 **Forced Logout Attack** 또는 **Session Termination Attack**이라고 볼 수 있습니다.

## ID TOKEN 로그아웃(RP-Initiated Logout) 기반 공격 시나리오

이를 Forced Logout Attack 또는 Session Termination Attack
누군가 타인의 ID Token을 탈취했다면, 아래처럼 `end_session_endpoint`를 직접 호출할 수 있습니다: [stackoverflow](https://stackoverflow.com/questions/74331444/keycloak-spring-security-oidc-id-token-hint-lifetime)

```
GET /end_session?
  id_token_hint=<탈취한 피해자의 ID Token>
  &post_logout_redirect_uri=https://attacker.com
```

IdP는 ID Token 안의 `sid`(session ID) 클레임을 보고 해당 세션을 종료해버립니다. 피해자는 갑자기 로그아웃되어 다시 로그인해야 하는 상황이 됩니다. [stackoverflow](https://stackoverflow.com/questions/74331444/keycloak-spring-security-oidc-id-token-hint-lifetime)

## 왜 위험한가 — OIDC 스펙의 허점

OIDC 스펙에서 특히 문제가 되는 부분이 있습니다: [github](https://github.com/jazzband/django-oauth-toolkit/issues/1293)

> *"The OP SHOULD accept ID Tokens **even when the exp time has passed**."*

즉, **만료된 ID Token도 로그아웃 목적으로는 유효하게 처리될 수 있어**, 공격 가능 시간 창이 넓어집니다.

## 방어 메커니즘

| 방어 수단 | 설명 |
|---|---|
| **IdP의 사용자 확인 프롬프트** | 로그아웃 전 "정말 로그아웃하시겠습니까?" UI 표시  [community.auth0](https://community.auth0.com/t/logout-api-does-not-require-id-token/35963) |
| **CSRF 토큰** | `end_session` 요청에 CSRF 토큰 필요  [connect2id](https://connect2id.com/products/server/docs/api/logout) |
| **`sid` 검증 강화** | 요청의 세션 컨텍스트와 `sid`가 불일치하면 의심 요청으로 처리  [github](https://github.com/jazzband/django-oauth-toolkit/issues/1293) |
| **`id_token_hint` 없는 요청 차단** | 반대로, hint 없는 요청을 DoS로 간주하여 차단  [community.auth0](https://community.auth0.com/t/logout-api-does-not-require-id-token/35963) |

## 실제 영향도

다만 이 공격은 **세션 종료(로그아웃 강제)**만 가능하고, 계정 탈취나 데이터 접근은 불가능합니다. 결국 **가용성(Availability)을 공격하는 DoS 성격**입니다. Keycloak처럼 `sid` 검증을 엄격히 수행하는 IdP에서는 세션 컨텍스트가 맞지 않으면 요청 자체를 거부할 수 있습니다. [stackoverflow](https://stackoverflow.com/questions/74331444/keycloak-spring-security-oidc-id-token-hint-lifetime)

> ⚠️ 결론적으로 ID Token은 **절대 클라이언트 외부에 노출되어서는 안 되며**, 브라우저 localStorage보다 **httpOnly 쿠키나 BFF(Backend For Frontend) 패턴**으로 보관하는 것이 안전합니다.