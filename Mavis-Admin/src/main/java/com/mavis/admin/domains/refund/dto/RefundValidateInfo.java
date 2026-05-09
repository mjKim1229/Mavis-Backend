package com.mavis.admin.domains.refund.dto;

import com.mavis.domain.domains.order.domain.RefundReceiveAccount;

public record RefundValidateInfo(
        Long refundId,
        Long orderId,
        int refundAmount,
        String refundReason,
        String paymentKey,
        RefundReceiveAccount refundReceiveAccount
) {}
