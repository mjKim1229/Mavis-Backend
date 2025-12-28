package com.mavis.api.order.dto;

import com.mavis.domain.domains.order.domain.PendingOrder;

public record PendingOrderRequest(
        String orderId,
        int amount
) {
    public PendingOrder toEntity() {
        return PendingOrder.builder()
                .orderId(orderId)
                .amount(amount)
                .build();
    }
}
