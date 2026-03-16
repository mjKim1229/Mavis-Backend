package com.mavis.domain.domains.order.domain;

import com.mavis.common.enums.EnumMapperType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum OrderStatus implements EnumMapperType {
    READY("주문 준비"),
    WAITING_FOR_DEPOSIT("입금 대기"),
    PAYMENT_CONFIRMED("결제 완료"), // 결제 완료 상태 추가
    ORDERED("발주 완료"), // 발주 완료 상태로 변경
    CANCELED("주문 취소");

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
