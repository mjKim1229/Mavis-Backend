package com.mavis.infrastructure.outer.api.tosspayments.dto;

import com.mavis.common.enums.EnumMapperType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TossPaymentMethod implements EnumMapperType {
    CARD("카드"),
    VIRTUAL_ACCOUNT("가상계좌"),
    EASYPAY("간편결제"),
    MOBILE_PAY("휴대폰"),
    BANK_TRANSFER("계좌이체"),
    GIFT_CARD("상품권");

    private final String kr;

    @Override
    public String getCode() {
        return name();
    }

    @Override
    public String getTitle() {
        return kr;
    }
}
