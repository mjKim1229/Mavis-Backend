package com.mavis.api.order.dto;

public record PaymentCancelInfo(
        Long orderId,
        String paymentKey
) {}
