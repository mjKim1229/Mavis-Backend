package com.mavis.infrastructure.outer.api.tosspayments.dto;

import java.time.LocalDateTime;

public record VirtualAccountDepositCallbackRequest(
        LocalDateTime createdAt,
        String secret,
        String status,
        String transactionKey,
        String orderId
) {
}
