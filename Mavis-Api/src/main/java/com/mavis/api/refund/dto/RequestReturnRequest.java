package com.mavis.api.refund.dto;

public record RequestReturnRequest(
        String refundReason,
        int refundQuantity,
        String trackingNumber
) {
}
