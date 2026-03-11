package com.mavis.admin.domains.order.dto;

import lombok.Builder;

@Builder
public record OrderItemInfo(
        String productName,
        String color,
        int quantity
) {
}
