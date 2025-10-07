package com.mavis.infrastructure.outer.api.tosspayments.dto;

import java.time.ZonedDateTime;

public record PaymentsCancels(
        Long cancelAmount,
        String cancelReason,
        Long taxFreeAmount,
        Long taxExceptionAmount,
        Long refundableAmount,
        Long easyPayDiscountAmount,
        ZonedDateTime canceledAt,
        String transactionKey

) {
}
