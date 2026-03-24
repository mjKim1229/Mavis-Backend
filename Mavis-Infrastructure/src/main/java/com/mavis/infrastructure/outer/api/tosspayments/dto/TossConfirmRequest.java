package com.mavis.infrastructure.outer.api.tosspayments.dto;

public record TossConfirmRequest(
        String paymentKey,
        String orderId,
        Long amount
) {
    public static TossConfirmRequest of(String paymentKey, String tossOrderId, Long amount) {
        return new TossConfirmRequest(paymentKey, tossOrderId, amount);
    }
}
