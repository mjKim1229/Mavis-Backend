package com.mavis.admin.domains.order.dto;

import com.mavis.common.enums.Size;
import com.mavis.domain.domains.order.dto.AdminOrderItemRow;
import lombok.Builder;

@Builder
public record OrderItemInfo(
        String productName,
        String color,
        Size size,
        int quantity
) {
    public static OrderItemInfo from(AdminOrderItemRow row) {
        return OrderItemInfo.builder()
                .productName(row.productName())
                .color(row.color())
                .size(row.size())
                .quantity(row.quantity())
                .build();
    }
}
