package com.mavis.api.order.dto;

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
}
