package com.mavis.domain.domains.order.dto;

import com.mavis.domain.domains.refund.domain.RefundStatus;
import com.mavis.domain.domains.refund.domain.RefundType;

public record OrderProductRow(
        Long orderId,
        Long orderItemId,
        Long productId,
        String productName,
        String color,
        int quantity,
        int price,
        RefundStatus refundStatus,
        RefundType refundType,
        String productImageUrl
) {
}
