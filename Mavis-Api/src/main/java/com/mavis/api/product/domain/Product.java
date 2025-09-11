package com.mavis.api.product.domain;

import com.mavis.api.common.jpa.BaseEntity;
import com.mavis.common.enums.ProductSubCategory;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Getter
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Integer price;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Enumerated(EnumType.STRING)
    private ProductSubCategory subCategory;

    @OneToMany
    private List<ProductImage> imageUrls;

    @Builder.Default
    private boolean isDeleted = false;
}
