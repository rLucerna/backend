-- ============================================================
-- DEV 전용 초기 데이터
-- dev 프로필에서만 로드됨 (application-dev.yml 설정)
-- Mock 인증 필터(DevMockJwtFilter)가 사용하는 사용자 레코드
-- ============================================================

-- Mock 개발 사용자 목록
-- DevMockJwtFilter: X-Mock-User 헤더로 user 전환 가능
-- X-Mock-User 헤더 없음 → dev-mock-keycloak-id (기본값)
-- X-Mock-User: dev-user-1 → dev-user-1 사용자로 테스트
-- X-Mock-User: dev-user-2 → dev-user-2 사용자로 테스트

INSERT INTO users (id, keycloak_id, email, status, last_login_at, created_at, updated_at)
SELECT nextval('users_id_seq'), 'dev-mock-keycloak-id', 'dev-mock-keycloak-id@lucerna.dev', 'ACTIVE', NOW(), NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE keycloak_id = 'dev-mock-keycloak-id');

INSERT INTO users (id, keycloak_id, email, status, last_login_at, created_at, updated_at)
SELECT nextval('users_id_seq'), 'dev-user-1', 'dev-user-1@lucerna.dev', 'ACTIVE', NOW(), NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE keycloak_id = 'dev-user-1');

INSERT INTO users (id, keycloak_id, email, status, last_login_at, created_at, updated_at)
SELECT nextval('users_id_seq'), 'dev-user-2', 'dev-user-2@lucerna.dev', 'ACTIVE', NOW(), NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE keycloak_id = 'dev-user-2');
