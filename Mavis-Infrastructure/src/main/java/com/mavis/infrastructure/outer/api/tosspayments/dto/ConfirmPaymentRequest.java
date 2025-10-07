package com.mavis.infrastructure.outer.api.tosspayments.dto;

public record ConfirmPaymentRequest(
        String paymentKey,
        String orderId,
        Long amount
) {
}
