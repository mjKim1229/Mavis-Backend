package com.mavis.domain.domains.order.domain;

import com.mavis.domain.domains.order.exception.UnsupportedPaymentMethodException;
import com.mavis.infrastructure.outer.api.tosspayments.dto.TossPaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentMethod {
    CARD("카드"),
    EASY_PAY("간편결제"),
    VIRTUAL_ACCOUNT("가상계좌"),
    MOBILE_PHONE("휴대폰"),
    TRANSFER("계좌이체"),
    CULTURE_GIFT_CERTIFICATE("문화상품권"),
    BOOK_GIFT_CERTIFICATE("도서문화상품권"),
    GAME_GIFT_CERTIFICATE("게임문화상품권"),
    DEFAULT("");

    private final String kr;   // UI 표시용 한글

    public static PaymentMethod from(TossPaymentMethod tossPaymentMethod) {
        if (tossPaymentMethod == null) {
            return DEFAULT;
        }

        try {
            return PaymentMethod.valueOf(tossPaymentMethod.name());
        } catch (IllegalArgumentException e) {
            throw UnsupportedPaymentMethodException.EXCEPTION;
        }
    }
}
