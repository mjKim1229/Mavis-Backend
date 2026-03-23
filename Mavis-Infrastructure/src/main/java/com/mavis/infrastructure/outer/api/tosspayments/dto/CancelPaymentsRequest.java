package com.mavis.infrastructure.outer.api.tosspayments.dto;

public record CancelPaymentsRequest(
        String cancelReason,
        Integer cancelAmount
) {
    public static CancelPaymentsRequest of(String cancelReason) {
        return new CancelPaymentsRequest(cancelReason, null);
    }
}
