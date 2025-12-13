package com.mavis.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.EnumSet;

import static com.mavis.common.enums.ProductSubCategory.*;

@RequiredArgsConstructor
public enum ProductCategory implements EnumMapperType {
    FASHION("패션", EnumSet.of(ACCESSORY, INNERWEAR)),
    FABRIC("원단", EnumSet.of(COTTON, POLY, NYLON, TENCEL)),
    EMBELLISHMENT("부자재", EnumSet.of(GARMENT_TRIMS, PACKAGING_MATERIALS)),
    FASHION_GOODS("잡화", EnumSet.noneOf(ProductSubCategory.class));

    private final String title;
    @Getter
    private final EnumSet<ProductSubCategory> subCategories;

    @Override
    public String getCode() {
        return name();
    }

    @Override
    public String getTitle() {
        return title;
    }

    public static ProductCategory fromSubCategory(ProductSubCategory subCategory) {
        return Arrays.stream(values())
                .filter(productCategory -> productCategory.getSubCategories().contains(subCategory))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Category Not Found"));
    }

}
