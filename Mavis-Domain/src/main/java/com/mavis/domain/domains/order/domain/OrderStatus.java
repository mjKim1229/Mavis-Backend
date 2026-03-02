package com.mavis.domain.domains.order.domain;

import com.mavis.common.enums.EnumMapperType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum OrderStatus implements EnumMapperType {
    ORDERED("주문 완료"),
    CANCELED("주문 취소"),
    CONFIRMED("주문 확인 (발주 완료)");

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
