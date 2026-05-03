package com.mavis.infrastructure.outer.api.tosspayments.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum EasyPayCode {
    TOSSPAY("토스페이", "TOSSPAY"),
    NAVERPAY("네이버페이", "NAVERPAY"),
    SAMSUNGPAY("삼성페이", "SAMSUNGPAY"),
    APPLEPAY("애플페이", "APPLEPAY"),
    LPAY("엘페이", "LPAY"),
    KAKAOPAY("카카오페이", "KAKAOPAY"),
    PINPAY("핀페이", "PINPAY"),
    PAYCO("페이코", "PAYCO"),
    SSG("SSG페이", "SSG");

    private final String kr;
    private final String en;

    @JsonCreator
    public static EasyPayCode fromKr(String kr) {
        for (EasyPayCode easyPayCode : EasyPayCode.values()) {
            if (easyPayCode.kr.equals(kr)) {
                return easyPayCode;
            }
        }
        return null;
    }
}
