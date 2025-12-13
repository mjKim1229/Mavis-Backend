package com.mavis.common.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ProductSubCategory implements EnumMapperType {
    ACCESSORY("액세서리"),
    INNERWEAR("이너웨어"),
    COTTON("면"),
    POLY("폴리"),
    NYLON("나일론"),
    TENCEL("텐셀"),
    GARMENT_TRIMS("의류 부자재"),
    PACKAGING_MATERIALS("포장 부자재");

    private final String title;

    @Override
    public String getCode() {
        return name();
    }

    @Override
    public String getTitle() {
        return title;
    }
}
