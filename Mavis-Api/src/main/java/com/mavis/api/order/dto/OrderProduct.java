package com.mavis.api.order.dto;

import com.mavis.domain.domains.order.domain.OrderOption;
import com.mavis.domain.domains.order.dto.OrderProductRow;
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
    public static OrderProduct from(OrderProductRow row) {
        RefundStatus refundStatus = row.refundStatus();
        String refundStatusTitle = refundStatus != null ? refundStatus.getTitle() : null;
        OrderOption option = new OrderOption(row.color(), row.quantity());
        int totalPrice = row.price() * row.quantity();
        return new OrderProduct(
                row.orderItemId(),
                row.productId(),
                row.productName(),
                option,
                totalPrice,
                refundStatus,
                refundStatusTitle,
                row.productImageUrl()
        );
    }
}
