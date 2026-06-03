package com.mavis.admin.domains.order.dto;

import com.mavis.domain.domains.order.dto.AdminOrderItemRow;
import lombok.Builder;

@Builder
public record OrderItemInfo(
        String productName,
        String color,
        int quantity
) {
    public static OrderItemInfo from(AdminOrderItemRow row) {
        return OrderItemInfo.builder()
                .productName(row.productName())
                .color(row.color())
                .quantity(row.quantity())
                .build();
    }
}
