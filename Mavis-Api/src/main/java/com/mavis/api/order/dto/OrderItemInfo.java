package com.mavis.api.order.dto;

import com.mavis.common.enums.Size;
import com.mavis.domain.domains.order.domain.OrderItem;
import lombok.Builder;

@Builder
public record OrderItemInfo(
        String productName,
        String color,
        Size size,
        int quantity
) {
    public static OrderItemInfo from(OrderItem orderItem) {
        return OrderItemInfo.builder()
                .productName(orderItem.getProduct().getName())
                .color(orderItem.getColor())
                .size(orderItem.getSize())
                .quantity(orderItem.getQuantity())
                .build();
    }
}
