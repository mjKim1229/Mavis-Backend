package com.mavis.admin.domains.refund.dto;

public record RefundValidateInfo(
        Long refundId,
        Long orderId,
        int refundAmount,
        String refundReason,
        String paymentKey,
        String refundReceiveBankCode,
        String refundReceiveAccountNumber,
        String refundReceiveHolderName
) {}
