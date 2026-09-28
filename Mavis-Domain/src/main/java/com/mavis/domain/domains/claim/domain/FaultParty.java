package com.mavis.domain.domains.claim.domain;

import com.mavis.common.enums.EnumMapperType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum FaultParty implements EnumMapperType {
    BUYER("구매자 귀책"),
    SELLER("판매자 귀책");

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
