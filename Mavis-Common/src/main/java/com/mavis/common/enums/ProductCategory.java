package com.mavis.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.EnumSet;

import static com.mavis.common.enums.ProductSubCategory.*;

@RequiredArgsConstructor
public enum ProductCategory implements EnumMapperType {
    EMBROIDERY("자수원단", EnumSet.of(COTTON, COTTON_MESH, COTTON_CHIFFON, COTTON_POLY_PATCH, CHIFFON, TENCEL)),
    SPECIAL_EMBROIDERY("특수자수원단", EnumSet.of(THREE_D_PATCH, COLOR, HAND, SPANGLE)),
    LACE("레이스", EnumSet.of(COTTON_LACE, COTTON_MESH_LACE, POLY_LACE)),
    EMBELLISHMENT("부자재", EnumSet.noneOf(ProductSubCategory.class));

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

}
