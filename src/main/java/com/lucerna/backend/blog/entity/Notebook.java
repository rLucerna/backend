package com.lucerna.backend.blog.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "notebooks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Notebook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)//ID번호 1,2,3.. 순서대로 1씩 증가시키면서 넣어줘
    private Long id; // 수첩 식별자 (PK)

    /*
     * TODO: 추후 Lodge(오두막) 엔티티가 만들어지면 연관관계 매핑(@ManyToOne)으로 변경 가능.
     * 현재는 ERD에 맞춰 BIGINT(Long) 타입으로 연결만 해둡니다.
     */
    @Column(name = "lodge_id", nullable = false)
    private Long lodgeId;

    @Column(nullable = false, length = 100)
    private String name; // 수첩 이름

    @Column(name = "color_code", length = 7)
    private String colorCode; // 수첩 색깔 (예: #FFFFFF)

    @Column(name = "display_order")
    private Integer displayOrder; // 정렬 순서

    @Enumerated(EnumType.STRING) // DB에 숫자가 아닌 'ALL' 같은 문자로 저장되게 함
    @Column(name = "visibility_status", nullable = false)
    private VisibilityStatus visibilityStatus = VisibilityStatus.ALL; // 기본값 ALL

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt; // 생성 시간

    @Column(name = "collection_sum", nullable = false)
    private int collectionSum = 0; // 수집 합 (기본값 0)

    @Builder
    public Notebook(Long lodgeId, String name, String colorCode, Integer displayOrder, VisibilityStatus visibilityStatus, int collectionSum) {
        this.lodgeId = lodgeId;
        this.name = name;
        this.colorCode = colorCode;
        this.displayOrder = displayOrder;
        this.visibilityStatus = visibilityStatus != null ? visibilityStatus : VisibilityStatus.ALL;
        this.collectionSum = collectionSum;
    }
}