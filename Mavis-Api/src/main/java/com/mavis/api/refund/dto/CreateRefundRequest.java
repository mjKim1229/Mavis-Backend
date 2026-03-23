package com.mavis.api.refund.dto;

import com.mavis.domain.domains.refund.domain.RefundReason;

public record CreateRefundRequest(
        RefundReason refundReason,
        String refundReasonDetail,
        int refundQuantity
) {
}
