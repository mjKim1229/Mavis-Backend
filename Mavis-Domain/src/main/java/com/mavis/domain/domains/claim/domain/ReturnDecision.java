package com.mavis.domain.domains.claim.domain;

import com.mavis.common.enums.EnumMapperType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ReturnDecision implements EnumMapperType {
    FULL_REFUND("전액 환불"),
    PARTIAL_REFUND("일부 환불"),
    NOT_SELLER_FAULT("귀책 아님");

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
