package com.mavis.api.order.dto;

import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.order.domain.OrderOption;
import com.mavis.domain.domains.refund.domain.RefundStatus;


public record OrderProduct(
        Long orderItemId,
        Long productId,
        String productName,
        OrderOption option,
        int totalPrice,
        RefundStatus refundStatus,
        String refundStatusTitle,
        String productImageUrl
) {
    public static OrderProduct from(OrderItem orderItem, RefundStatus refundStatus) {
        String imageUrl = orderItem.getProduct().getMainImageUrl();
        return new OrderProduct(
                orderItem.getId(),
                orderItem.getProduct().getId(),
                orderItem.getProduct().getName(),
                new OrderOption(orderItem.getColor(), orderItem.getQuantity()),
                orderItem.getTotalPrice(),
                refundStatus,
                refundStatus != null ? refundStatus.getTitle() : null,
                imageUrl
        );
    }
}
