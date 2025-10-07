package com.mavis.infrastructure.outer.api.tosspayments.dto;

public record PaymentsEasyPay(
        EasyPayCode provider,
        Long amount,
        Long discountAmount
) {
}
