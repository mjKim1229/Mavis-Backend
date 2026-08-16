package com.mavis.api.order.dto;

import com.mavis.domain.domains.order.domain.OrderOption;
import com.mavis.domain.domains.order.dto.OrderProductRow;
import com.mavis.domain.domains.refund.domain.RefundStatus;
import com.mavis.domain.domains.refund.domain.RefundType;


public record OrderProduct(
        Long orderItemId,
        Long productId,
        String productName,
        OrderOption option,
        int totalPrice,
        RefundStatus refundStatus,
        String refundStatusTitle,
        Integer refundAmount,
        String productImageUrl
) {
    public static OrderProduct from(OrderProductRow row) {
        RefundStatus refundStatus = row.refundStatus();
        String refundStatusTitle = refundStatus != null ? refundStatus.getTitle() : null;
        OrderOption option = new OrderOption(row.color(), row.quantity());
        int totalPrice = row.price();
        Integer refundAmount = row.refundType() == RefundType.RETURN ? row.price() : null;
        return new OrderProduct(
                row.orderItemId(),
                row.productId(),
                row.productName(),
                option,
                totalPrice,
                refundStatus,
                refundStatusTitle,
                refundAmount,
                row.productImageUrl()
        );
    }
}
