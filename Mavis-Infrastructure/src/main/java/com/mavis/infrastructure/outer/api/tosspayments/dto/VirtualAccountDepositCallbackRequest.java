package com.mavis.infrastructure.outer.api.tosspayments.dto;

import java.time.ZonedDateTime;

public record VirtualAccountDepositCallbackRequest(
        ZonedDateTime createdAt,
        String secret,
        String status,
        String transactionKey,
        String orderId
) {
}
