package com.mavis.common.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ProductSubCategory implements EnumMapperType {
    COTTON("면 자수"),
    COTTON_MESH("면 망사 자수"),
    COTTON_CHIFFON("면 샤 자수"),
    COTTON_POLY_PATCH("면 폴리패치 자수"),
    CHIFFON("쉬폰 자수"),
    TENCEL("텐셀 자수"),
    THREE_D_PATCH("입체 패치 자수"),
    COLOR("컬러 자수"),
    HAND("핸들 자수"),
    SPANGLE("스팡글 자수"),
    COTTON_LACE("면 레이스"),
    COTTON_MESH_LACE("면 망사 레이스"),
    POLY_LACE("폴리 레이스");

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
