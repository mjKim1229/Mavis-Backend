package com.mavis.infrastructure.outer.api.tosspayments.dto;

public record PaymentsRefundReceiveAccount(
        String bankCode,
        String accountNumber,
        String holderName
) {
}
