package com.mavis.api.order.dto;

import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.order.domain.OrderOption;
import lombok.Builder;

@Builder
public record GetOrderItemUserCanReviewResponse(
        Long orderItemId,
        String productName,
        OrderOption orderOption,
        String orderedAt
) {
    public static GetOrderItemUserCanReviewResponse from(OrderItem orderItem) {
        return GetOrderItemUserCanReviewResponse.builder()
                .orderItemId(orderItem.getId())
                .productName(orderItem.getProduct().getName())
                .orderOption(new OrderOption(orderItem.getColor(), orderItem.getQuantity()))
                .build();
    }
}
