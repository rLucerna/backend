# Lucerna 프로젝트 기능 추가 계획

---

## [FUTURE] Option C 기반 소셜 인증 기능 추가

> 현재 PKCE 인증 서비스 개발 완료 후 추가 예정.
> Option C 아키텍처 기반 — Spring Boot가 소셜 토큰을 수신하여 Keycloak Token Exchange 수행.

---

### 개요

React Native 앱에서 네이티브 SDK로 소셜 로그인 후 획득한 소셜 토큰을
Spring Boot를 통해 Keycloak 토큰으로 교환하는 흐름.

지원 예정 Provider: Google, Apple, Kakao (우선순위 순)

---

### 인증 흐름

```
앱
 └─(1) 네이티브 SDK로 소셜 로그인 (Google/Apple/Kakao)
 └─(2) 소셜 토큰(ID Token 또는 Access Token) 획득

앱 → Spring Boot
 └─ POST /auth/social/token
    { provider: "google", token: "<소셜_토큰>" }

Spring Boot → Keycloak
 └─ POST /realms/lucerna_test/protocol/openid-connect/token
    grant_type          = urn:ietf:params:oauth:grant-type:token-exchange
    client_id           = lucerna-service
    client_secret       = <secret>
    subject_token       = <소셜_토큰>
    subject_token_type  = urn:ietf:params:oauth:token-type:id_token
    subject_issuer      = google  (또는 apple, kakao)
    requested_token_type= urn:ietf:params:oauth:token-type:refresh_token

Keycloak → Spring Boot
 └─ { access_token, refresh_token, id_token }

Spring Boot → 앱
 └─ 동일한 TokenResponse 반환 (PKCE 응답 형식과 동일)
```

---

### 필요 엔드포인트

| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/auth/social/token` | 소셜 토큰 → Keycloak 토큰 교환 |
| POST | `/auth/refresh` | Refresh Token으로 토큰 갱신 (PKCE와 공통 사용) |

---

### 필요한 Spring Boot 구성 요소

#### DTO

```
SocialTokenRequest.java
  - String provider   (google / apple / kakao)
  - String token

TokenResponse.java     ← PKCE와 공통 사용, 신규 추가 불필요
```

#### Service

```
SocialTokenService.java
  - exchangeToken(SocialTokenRequest request) : TokenResponse
    └─ provider 값으로 subject_issuer 매핑
    └─ Keycloak Token Exchange 엔드포인트 호출
    └─ 응답을 TokenResponse로 변환하여 반환

ProviderValidator.java  (선택)
  - 지원 Provider 화이트리스트 검증
  - 지원하지 않는 provider 요청 시 400 반환
```

#### Controller

```
AuthController.java  ← 기존 PKCE 컨트롤러에 엔드포인트 추가
  + POST /auth/social/token → SocialTokenService.exchangeToken()
```

#### Config

```
KeycloakConfig.java  ← 기존 PKCE용 설정 재사용
  - lucerna-service Client Secret 환경변수로 주입
  - Token Exchange 엔드포인트 URL 설정

application.yml 추가 항목:
  keycloak:
    social:
      google-issuer: google
      apple-issuer: apple
      kakao-issuer: kakao
```

---

### Provider별 토큰 타입 차이

| Provider | 전달 토큰 | `subject_token_type` |
|----------|---------|----------------------|
| Google | ID Token (JWT) | `id_token` |
| Apple | ID Token (JWT) | `id_token` |
| Kakao | Access Token (불투명) | `access_token` |

Kakao는 JWT가 아닌 불투명 토큰이므로 Keycloak이 Kakao API를 직접 호출하여 검증.

---

### Keycloak 사전 설정 필요 항목

1. **Token Exchange 기능 활성화** (`values.yaml`)
   ```yaml
   extraEnv: |
     - name: KC_FEATURES
       value: token-exchange
   ```

2. **Identity Provider 등록** (Realm → Identity Providers)
   - Google / Apple / Kakao 각각 설정

3. **lucerna-service 클라이언트에 Token Exchange 권한 부여**

4. **Identity Provider Mapper 설정** - 소셜 계정 속성 → Keycloak 사용자 속성 매핑

---

### 고려 사항

- **신규 vs 기존 사용자**: 동일 이메일 소셜 계정의 Keycloak 계정 연동 정책 결정 필요.
- **Apple 로그인**: 난이도 높음 (Key ID, Team ID, .p8 파일 필요). 우선순위 Google → Kakao → Apple 권장.
- **Rate Limiting**: `/auth/social/token`에 별도 Rate Limit 적용 권장.
- **소셜 연동 해제**: `DELETE /auth/social/unlink` 추후 추가 (Keycloak Admin API 사용).
- **Refresh Token 전략**: 소셜 도입 시 앱 노출 vs 서버 관리 재검토 권장.

---

### 아키텍처 다이어그램

```
┌─────────────────────────────────────────────────────┐
│                   React Native App                  │
│  [PKCE 흐름]              [소셜 흐름]                │
│  Keycloak 로그인 페이지    Google/Apple/Kakao SDK    │
│  auth_code 수신           소셜 토큰 수신             │
└──────────────┬───────────────────┬──────────────────┘
               │                   │
               ▼                   ▼
┌─────────────────────────────────────────────────────┐
│                   Spring Boot                       │
│  POST /auth/token          POST /auth/social/token  │
│  PkceTokenService          SocialTokenService       │
│       └──────────────┬─────────────────┘           │
│                      ▼                              │
│             Keycloak HTTP Client                    │
└──────────────────────┬──────────────────────────────┘
                       ▼
┌─────────────────────────────────────────────────────┐
│                    Keycloak                         │
│  lucerna-app (Public)   lucerna-service (+ IdP 연동)│
└─────────────────────────────────────────────────────┘
```
