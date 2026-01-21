package com.mavis.domain.domains.order.domain;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentMethod {
    // 간편결제
    EASYPAY("EASYPAY", "간편 결제"),
    // 카드결제
    CARD("CARD", "카드 결제"),
    // 결제방식 미지정상태
    DEFAULT("DEFAULT", "");
    private String value;
    private String kr;
}
