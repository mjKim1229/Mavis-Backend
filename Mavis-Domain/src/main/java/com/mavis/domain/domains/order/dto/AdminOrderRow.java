package com.mavis.domain.domains.order.dto;

import java.time.LocalDateTime;

public record AdminOrderRow(
        Long orderId,
        String tossOrderId,
        String receiverName,
        String receiverPhone,
        String address,
        String addressMemo,
        String buyerName,
        LocalDateTime createdAt,
        int totalPrice
) {
}
