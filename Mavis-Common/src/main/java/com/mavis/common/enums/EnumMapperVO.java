package com.mavis.common.enums;

public record EnumMapperVO(
        String code,
        String title
) {
    public EnumMapperVO(EnumMapperType enumMapperType) {
        this(enumMapperType.getCode(), enumMapperType.getTitle());
    }
}
