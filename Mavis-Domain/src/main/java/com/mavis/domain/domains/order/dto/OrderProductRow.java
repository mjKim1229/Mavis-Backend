package com.mavis.domain.domains.order.dto;

import com.mavis.domain.domains.refund.domain.RefundStatus;

public record OrderProductRow(
        Long orderId,
        Long orderItemId,
        Long productId,
        String productName,
        String color,
        int quantity,
        int price,
        RefundStatus refundStatus,
        String productImageUrl
) {
}
