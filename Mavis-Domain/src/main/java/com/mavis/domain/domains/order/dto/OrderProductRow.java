package com.mavis.domain.domains.order.dto;

import com.mavis.domain.domains.claim.domain.ClaimStatus;

public record OrderProductRow(
        Long orderId,
        Long orderItemId,
        Long productId,
        String productName,
        String color,
        int quantity,
        int totalPrice,
        ClaimStatus refundStatus,
        Integer refundAmount,
        String productImageUrl
) {
}
