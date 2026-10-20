package com.mavis.domain.domains.order.dto;

import com.mavis.common.enums.Size;

public record AdminOrderItemRow(
        Long orderId,
        String productName,
        String color,
        Size size,
        int quantity
) {
}
