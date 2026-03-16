package com.lucerna.backend.user.entity;

import com.lucerna.backend.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 서비스 사용자 엔티티.
 * Keycloak에서 인증된 사용자의 서비스 DB 레코드.
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_id_seq")
    @SequenceGenerator(name = "users_id_seq", sequenceName = "users_id_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false, unique = true)
    private String keycloakId;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    private LocalDateTime lastLoginAt;

    private User(String keycloakId, String email, UserStatus status) {
        this.keycloakId = keycloakId;
        this.email = email;
        this.status = status;
        this.lastLoginAt = LocalDateTime.now();
    }

    /** 신규 사용자 생성 팩토리 메서드 */
    public static User createNewUser(String keycloakId, String email) {
        return new User(keycloakId, email, UserStatus.ACTIVE);
    }

    /** 로그인 시각 갱신 (dirty checking) */
    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    /**
     * 회원 탈퇴 처리 (Soft Delete).
     * - status = DELETED → 서비스 접근 즉시 차단
     * - email은 30일 후 배치에서 마스킹 (유예 기간 내 복구 가능성 보존)
     * - keycloakId 유지 → 30일 배치 시 Keycloak 사용자 완전 삭제에 필요
     */
    public void withdraw() {
        this.status = UserStatus.DELETED;
    }

    /**
     * 개인정보 마스킹 — 30일 배치에서 호출.
     * Hard Delete 직전 식별 불가 처리.
     */
    public void maskPersonalInfo() {
        this.email = "deleted_" + this.id + "@deleted.invalid";
    }
}
