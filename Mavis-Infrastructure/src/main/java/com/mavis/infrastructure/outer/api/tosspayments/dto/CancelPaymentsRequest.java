package com.mavis.infrastructure.outer.api.tosspayments.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CancelPaymentsRequest(
        String cancelReason,
        Integer cancelAmount,
        RefundReceiveAccountRequest refundReceiveAccount
) {
    public static CancelPaymentsRequest of(String cancelReason) {
        return new CancelPaymentsRequest(cancelReason, null, null);
    }
}
