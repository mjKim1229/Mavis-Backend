package com.mavis.domain.domains.order.dto;

import com.mavis.common.enums.Size;
import com.mavis.domain.domains.claim.domain.ClaimStatus;

public record OrderProductRow(
        Long orderId,
        Long orderItemId,
        Long productId,
        String productName,
        String color,
        Size size,
        int quantity,
        int totalPrice,
        ClaimStatus refundStatus,
        Integer refundAmount,
        String productImageUrl
) {
}
