package com.mavis.domain.domains.order.dto;

public record AdminOrderItemRow(
        Long orderId,
        String productName,
        String color,
        int quantity
) {
}
