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
        RefundStatus refundStatus
) {
    public static OrderProduct from(OrderItem orderItem) {
        RefundStatus refundStatus = orderItem.getRefund() != null
                ? orderItem.getRefund().getRefundStatus()
                : null;
        return new OrderProduct(
                orderItem.getId(),
                orderItem.getProduct().getId(),
                orderItem.getProduct().getName(),
                new OrderOption(orderItem.getColor(), orderItem.getQuantity()),
                orderItem.getPrice() * orderItem.getQuantity(),
                refundStatus
        );
    }
}
