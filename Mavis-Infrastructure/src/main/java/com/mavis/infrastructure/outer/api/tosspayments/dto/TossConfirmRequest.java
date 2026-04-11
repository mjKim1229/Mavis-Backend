package com.mavis.infrastructure.outer.api.tosspayments.dto;

public record TossConfirmRequest(
        String paymentKey,
        String orderId,
        int amount
) {
    public static TossConfirmRequest of(String paymentKey, String tossOrderId, int amount) {
        return new TossConfirmRequest(paymentKey, tossOrderId, amount);
    }
}
