package com.mavis.domain.domains.product.domain;

import com.mavis.domain.domains.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Entity
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductImage extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    private ProductImageType imageType;

    private int orderNum;

    @Builder.Default
    private boolean isDeleted = false;

    public void update(int orderNum) {
        this.orderNum = orderNum;
    }

    public void delete() {
        this.isDeleted = true;
    }
}
