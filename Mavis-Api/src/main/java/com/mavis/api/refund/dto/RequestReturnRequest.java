package com.mavis.api.refund.dto;

public record RequestReturnRequest(
        String refundReason,
        String trackingNumber
) {
}
