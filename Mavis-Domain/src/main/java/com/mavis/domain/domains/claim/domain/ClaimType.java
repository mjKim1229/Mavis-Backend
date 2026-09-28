package com.mavis.domain.domains.claim.domain;

import com.mavis.common.enums.EnumMapperType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ClaimType implements EnumMapperType {
    CANCEL("주문 취소"),
    RETURN("반품");

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
