package com.mavis.api.order.dto;

import com.mavis.infrastructure.outer.api.tosspayments.dto.TossPaymentMethod;

public record ConfirmPaymentResponse(
        TossPaymentMethod method
) {
    public static ConfirmPaymentResponse from(TossPaymentMethod method) {
        return new ConfirmPaymentResponse(method);
    }
}
