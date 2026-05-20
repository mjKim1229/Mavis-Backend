package com.mavis.domain.domains.review.domain;

import com.mavis.domain.domains.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ReviewImage extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private Review review;

    private int sortOrder;

    @Builder.Default
    private boolean isDeleted = false;

    public void update(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void delete() {
        this.isDeleted = true;
    }
}
