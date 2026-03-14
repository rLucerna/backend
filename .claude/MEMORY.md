# 루체르나 백엔드 프로젝트 메모리

## 프로젝트 개요
- **서비스명**: 루체르나 (Lucerna)
- **개념**: 자선 활동과 기록 중심의 가상 커뮤니티형 플랫폼 (앱)
- **개발 기간**: 2026.3.1 ~ 2026.8 / 인력: 백엔드 3명, 프론트 3명, 인프라 1명(백엔드 겸직)

## 기술 스택
- Java 21, Spring Boot 3.5.11
- PostgreSQL (lucerna_db, localhost:5432), Redis
- Keycloak (OAuth2 Resource Server / JWT)
- Docker, Kubernetes (로컬: minikube → 추후 AWS 클라우드)
- JPA/Hibernate, QueryDSL, Lombok
- 테스트: H2(in-memory), JUnit

## 프로젝트 구조 (루트)
- `src/main/java/com/lucerna/backend/` - 메인 소스
- `src/main/resources/application.yml` - 설정
- `src/test/resources/application-test.yml` - 테스트 설정

## 개발 원칙 (CLAUDE.md 기준)
- Layered Architecture: Controller → Service → Repository
- 공통 응답: `CommonResponse<T>` 래퍼
- Global Exception Handler 집중 처리
- N+1 주의, 비동기/캐싱 전략 우선 고려
- 테스트 코드 포함 필수 (JUnit)
- 오버 엔지니어링 금지

## 현재 상태 (feature/auth 브랜치)
- PKCE 인증 완전 구현 및 통합 테스트 완료
- `/auth/token-dev`: CommonResponse 제거 → TokenResponse 직접 반환 (OAuth2 RFC 6749 준수, Postman 호환)
- 구현 완료: CommonResponse, ErrorCode, BusinessException, GlobalExceptionHandler, SecurityConfig, WebConfig, JpaConfig, BaseEntity
- 테스트: 전체 통과

## 주요 참고 문서 (프로젝트 내 `.claude/` 디렉터리)
- `.claude/FEATURELOG.md` - 기능 작업 현황
- `.claude/Plans.md` - 소셜 인증 계획 (PKCE 이후)
- `.claude/architecture.md` - 패키지 구조, 도메인 설계, 테스트 패턴
- `.claude/Modify_LOG.md` - 코드 수정 이력
- `.claude/service.md` - 서비스 소개

## 테스트 패턴 (중요)
- `GlobalExceptionHandler` 테스트: `MockMvcBuilders.standaloneSetup()` 사용 (`@WebMvcTest` inner class 라우트 미등록 문제)
- Controller 테스트: `@WebMvcTest` + `excludeAutoConfiguration = {SecurityAutoConfiguration, OAuth2ResourceServerAutoConfiguration}` + `excludeFilters = SecurityConfig` → CSRF 403 문제 방지
  - `OAuth2ResourceServerAutoConfiguration`을 제거하지 않으면 자체 필터 체인 생성 → CSRF 활성화 → 403 발생
- Service 단위 테스트: `@WireMockTest` (Spring 컨텍스트 없이 RestClient + WireMock 조합)

## feature/auth 브랜치 완료 내용
- `auth/config/KeycloakProperties.java` (@ConfigurationProperties("keycloak"))
- `auth/dto/TokenRequest, RefreshRequest, TokenResponse.java`
- `auth/service/PkceTokenService.java` (RestClient 기반, Keycloak 토큰 교환)
- `auth/controller/AuthController.java` (POST /auth/token, POST /auth/refresh, POST /auth/token-dev)
- 테스트 13개 전체 통과

## PKCE 인증 디버깅 이력 (중요)
- **문제 1**: RestClient의 Jackson converter가 인덱스 0에 위치 → `MultiValueMap`을 JSON으로 직렬화 → Keycloak `Missing form parameter: grant_type` 에러
  - 해결: converter 순서 변경 대신, 기본 converter 목록 유지 (FormHttpMessageConverter가 먼저 동작)
- **문제 2**: Keycloak 응답 Content-Type이 `application/octet-stream` → Spring RestClient가 파싱 실패
  - 해결: `.body(String.class)`로 받고 `ObjectMapper.readValue()`로 직접 파싱
- **문제 3**: Java 21 기본 JDK HttpClient가 Keycloak 응답 읽기 실패
  - 해결: `SimpleClientHttpRequestFactory` (HttpURLConnection 기반) 명시적 사용
- **Keycloak admin**: username=admin, password=admin-password (KC_BOOTSTRAP_ADMIN_PASSWORD)
- **테스트 사용자**: zhzhqkq0226@gmail.com (password: test1234로 리셋함)

## 서비스 기능 (5가지)
1. 자선활동 검색 - 공공데이터 포털 Open API (봉사, 기부 정보)
2. 자선활동 관리 - 사용자 기부/봉사 조회·관리
3. 글쓰기(수첩) - 자선활동 경험, 일상, 성찰 글쓰기
4. 오두막 - 싸이월드 미니홈피 형태의 폐쇄형 가상 커뮤니티
5. 등불 - 기름(기부)+성냥(봉사) 아이템으로 오두막 등불 점등

## 인프라 구조 (infra/ 디렉터리)
- 모노레포: `lucerna/` → `lucerna-backend/`, `infra/`, `manifest/`
- 개발 환경: Spring Boot 로컬 실행 + PostgreSQL/Keycloak Docker Compose
- 프로덕션 목표: GCP (Cloud Run + Cloud SQL + GKE)
- Docker Compose Override 패턴: base → dev/prod → member(개인)
- 포트: Spring Boot 8081, PostgreSQL 5432, Keycloak 8080

## 다음 구현 예정 기능 (순서대로)
1. 사용자 정보 조회
2. 로그아웃
3. 회원 탈퇴

## 상세 내용
- `.claude/architecture.md` 참고 (패키지 구조, 테스트 패턴)