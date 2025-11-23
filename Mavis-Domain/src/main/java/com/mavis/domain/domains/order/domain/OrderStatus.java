package com.mavis.domain.domains.order.domain;

import com.mavis.common.enums.EnumMapperType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum OrderStatus implements EnumMapperType {
    PROCESSING("주문 처리중"),
    SHIPPED("배송중"),
    DELIVERED("배송 완");

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
