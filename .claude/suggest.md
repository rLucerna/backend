# 제안 이력

---

## [2026-03-13] Onboarding API 전략 제안

### 배경
FEAT-003에서 `GET /users/me` JIT 생성 시 `users` 레코드만 생성.
Lodge(프로필), user_app_conf(설정)은 별도 API에서 생성해야 함.

### 제안

1. **Onboarding API** (`PATCH /api/v1/users/me/lodge`)
   - 닉네임, 프로필 이미지, 소개 등록
   - 클라이언트: `isNewUser || lodge == null`이면 Onboarding 화면 진입
   - 앱 재시작 시에도 lodge 없으면 Onboarding 유지

2. **설정 API** (`PUT /api/v1/users/me/settings`)
   - `user_app_conf` JSONB 컬럼으로 앱 설정 저장
   - 알림, 테마, 언어 등

3. **단계적 분리 이유**
   - JIT 시점에 모든 데이터를 만들면 불필요한 기본값 레코드 다수 생성
   - 사용자가 실제로 입력한 데이터만 DB에 저장 → 데이터 품질 유지
   - Onboarding 미완료 사용자 추적 가능 (`lodge IS NULL`)

### 상태
미채택 — FEAT-003 구현 이후 검토 예정
