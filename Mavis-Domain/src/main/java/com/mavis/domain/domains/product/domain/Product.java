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

    @OneToMany(mappedBy = "product")
    @Builder.Default
    @SQLRestriction("is_deleted = false")
    @OrderBy("orderNum ASC")
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

    /**
     * 판매 중인 색상인지 확인한다. 색상 옵션이 없는 상품은 빈 값만 허용한다.
     */
    public boolean supportsColor(String color) {
        List<String> activeColors = colors.stream()
                .filter(productColor -> !productColor.isDeleted())
                .map(ProductColor::getColor)
                .toList();
        boolean hasNoColorOption = activeColors.isEmpty();
        if (hasNoColorOption) {
            return color == null || color.isBlank();
        }
        return activeColors.contains(color);
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
