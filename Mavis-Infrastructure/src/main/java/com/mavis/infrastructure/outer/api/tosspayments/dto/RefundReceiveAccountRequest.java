package com.mavis.infrastructure.outer.api.tosspayments.dto;

public record RefundReceiveAccountRequest(
        String bank,
        String accountNumber,
        String holderName
) {
}
