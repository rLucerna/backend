-- ============================================================
-- DEV 전용 초기 데이터
-- dev 프로필에서만 로드됨 (application-dev.yml 설정)
-- Mock 인증 필터(DevMockJwtFilter)가 사용하는 사용자 레코드
-- ============================================================

-- Mock 개발 사용자 (DevMockJwtFilter의 MOCK_KEYCLOAK_ID와 일치해야 함)
INSERT INTO users (id, keycloak_id, email, status, last_login_at, created_at, updated_at)
VALUES (nextval('users_id_seq'), 'dev-mock-keycloak-id', 'dev@lucerna.test', 'ACTIVE', NOW(), NOW(), NOW())
ON CONFLICT (keycloak_id) DO NOTHING;
