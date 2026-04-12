package com.mavis.api.order.dto;

import com.mavis.domain.domains.order.domain.OrderItem;
import lombok.Builder;

@Builder
public record OrderItemInfo(
        String productName,
        String color,
        int quantity
) {
    public static OrderItemInfo from(OrderItem orderItem) {
        return OrderItemInfo.builder()
                .productName(orderItem.getProduct().getName())
                .color(orderItem.getColor())
                .quantity(orderItem.getQuantity())
                .build();
    }
}
