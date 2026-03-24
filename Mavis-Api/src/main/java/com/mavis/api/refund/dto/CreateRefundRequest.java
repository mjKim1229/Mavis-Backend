package com.mavis.api.refund.dto;

public record CreateRefundRequest(
        String refundReason,
        int refundQuantity
) {
}
