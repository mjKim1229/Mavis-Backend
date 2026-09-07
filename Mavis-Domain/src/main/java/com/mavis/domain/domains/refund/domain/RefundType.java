package com.mavis.domain.domains.refund.domain;

import com.mavis.common.enums.EnumMapperType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum RefundType implements EnumMapperType {
    CANCEL("주문 취소"),   // OrderStatus.PAYMENT_CONFIRMED 또는 WAITING_FOR_DEPOSIT 상태에서 허용
    RETURN("반품 신청");   // DeliveryStatus.DELIVERED 이후에만 허용

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
