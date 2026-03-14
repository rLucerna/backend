package com.lucerna.backend.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 프로필(Lodge) 엔티티.
 * Onboarding 완료 후 생성되며, JIT 시점에는 생성하지 않음.
 * BaseEntity 미상속 — 스키마에 audit 컬럼 없음.
 */
@Entity
@Table(name = "lodges")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Lodge {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lodges_id_seq")
    @SequenceGenerator(name = "lodges_id_seq", sequenceName = "lodges_id_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private String nickname;

    private String profileImageUrl;

    private String introduction;
}
