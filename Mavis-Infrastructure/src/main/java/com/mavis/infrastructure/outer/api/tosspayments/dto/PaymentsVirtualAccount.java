package com.mavis.infrastructure.outer.api.tosspayments.dto;

import java.time.ZonedDateTime;

public record PaymentsVirtualAccount(
        String accountType,
        String accountNumber,
        String bankCode,
        String customerName,
        String depositorName,
        ZonedDateTime dueDate,
        VirtualAccountRefundStatus refundStatus,
        Boolean expired,
        String settlementStatus,
        PaymentsRefundReceiveAccount refundReceiveAccount
) {
}
