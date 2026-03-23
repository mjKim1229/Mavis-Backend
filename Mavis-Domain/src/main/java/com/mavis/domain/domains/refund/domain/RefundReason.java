package com.mavis.domain.domains.refund.domain;

import com.mavis.common.enums.EnumMapperType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum RefundReason implements EnumMapperType {
    CHANGE_OF_MIND("단순 변심"),
    DEFECTIVE_PRODUCT("상품 불량"),
    WRONG_DELIVERY("오배송"),
    OUT_OF_STOCK("재고 부족"),
    OTHER("기타");

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
