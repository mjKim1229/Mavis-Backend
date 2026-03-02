package com.mavis.infrastructure.outer.api.tosspayments.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.mavis.common.enums.EnumMapperType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TossPaymentMethod implements EnumMapperType {
    CARD("카드"),
    EASY_PAY("간편결제"),
    VIRTUAL_ACCOUNT("가상계좌"),
    MOBILE_PHONE("휴대폰"),
    TRANSFER("계좌이체"),
    CULTURE_GIFT_CERTIFICATE("문화상품권"),
    BOOK_GIFT_CERTIFICATE("도서문화상품권"),
    GAME_GIFT_CERTIFICATE("게임문화상품권");

    private final String kr;

    @Override
    public String getCode() {
        return name();
    }

    @Override
    public String getTitle() {
        return kr;
    }

    @JsonCreator
    public static TossPaymentMethod fromKr(String kr) {
        if (kr == null) {
            return null;
        }

        for (TossPaymentMethod method : TossPaymentMethod.values()) {
            if (method.kr.equals(kr)) {
                return method;
            }
        }
        return null;
    }
}
