package com.mavis.api.order.dto;

public record PaymentCancelInfo(
        Long orderId,
        String paymentKey,
        String refundReceiveBankCode,
        String refundReceiveAccountNumber,
        String refundReceiveHolderName
) {}
