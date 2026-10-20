package com.mavis.domain.domains.product.domain;

import com.mavis.common.enums.Size;
import com.mavis.domain.domains.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Getter
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductSize extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Comment("판매 사이즈 (FREE, XS, S, M, L, XL, XXL)")
    @Column(columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    private Size size;

    @Builder.Default
    private boolean isDeleted = false;

    public static ProductSize of(Product product, Size size) {
        return ProductSize.builder()
                .product(product)
                .size(size)
                .build();
    }

    public void delete() {
        this.isDeleted = true;
    }
}
