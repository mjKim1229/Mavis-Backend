package com.mavis.common.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ProductSubCategory implements EnumMapperType {
    COTTON("면자수"),
    COTTON_MESH("면망사자수"),
    COTTON_CHIFFON("면샤자수"),
    COTTON_POLY_PATCH("면,폴리패치자수"),
    CHIFFON("쉬폰자수"),
    TENCEL("텐셀자수"),
    THREE_D_PATCH("입체패치자수"),
    COLOR("컬러자수"),
    HAND("핸들자수"),
    SPANGLE("스팡글자수"),
    COTTON_LACE("면레이스"),
    COTTON_MESH_LACE("면망사레이스"),
    POLY_LACE("폴리레이스");

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
