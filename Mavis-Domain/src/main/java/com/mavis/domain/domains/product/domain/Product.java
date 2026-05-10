package com.mavis.domain.domains.product.domain;

import com.mavis.common.enums.ProductSubCategory;
import com.mavis.domain.domains.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
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

    @Column(columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    private ProductSubCategory subCategory;

    @OneToOne(mappedBy = "product")
    private ProductNotice productNotice;

    @OneToMany(mappedBy = "product")
    @Builder.Default
    @SQLRestriction("is_deleted = false")
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    @Builder.Default
    @SQLRestriction("is_deleted = false")
    private List<ProductColor> colors = new ArrayList<>();

    @OneToMany(mappedBy = "product")
    @Builder.Default
    private List<ProductTotalView> totalViews = new ArrayList<>();

    @Builder.Default
    private boolean isDeleted = false;

    @Builder.Default
    private boolean isClearance = false;

    public String getMainImageUrl() {
        return images.stream()
                .filter(img -> img.getImageType() == ProductImageType.MAIN)
                .findFirst()
                .map(ProductImage::getImageUrl)
                .orElse(null);
    }

    public List<String> getColorNames() {
        return colors.stream()
                .map(ProductColor::getColor)
                .toList();
    }

    public void update(String name, Integer price, ProductSubCategory subCategory) {
        this.name = name;
        this.price = price;
        this.subCategory = subCategory;
    }

    public void updateClearance(boolean isClearance) {
        this.isClearance = isClearance;
    }

    public void delete() {
        isDeleted = true;
    }
}
