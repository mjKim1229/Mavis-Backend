package com.mavis.domain.domains.claim.domain;

import com.mavis.common.enums.EnumMapperType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ClaimReasonType implements EnumMapperType {
    CHANGE_OF_MIND("단순 변심"),
    SELLER_FAULT("판매자 귀책");

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
