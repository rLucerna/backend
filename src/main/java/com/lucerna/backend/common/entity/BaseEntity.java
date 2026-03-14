package com.lucerna.backend.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 모든 엔티티의 공통 부모 클래스.
 * 생성일시, 수정일시를 자동 관리.
 *
 * 사용 예: public class User extends BaseEntity { ... }
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    /** 엔티티 최초 생성 시각 (자동 설정, 수정 불가) */
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /** 엔티티 마지막 수정 시각 (자동 갱신) */
    @LastModifiedDate
    private LocalDateTime updatedAt;
}