package com.mavis.infrastructure.outer.api.tosspayments.dto;

public record PaymentsCashReceipt(
        String receiptKey,
        String type,
        Long amount,
        Long taxFreeAmount,
        String issueNumber,
        String receiptUrl
) {
}
