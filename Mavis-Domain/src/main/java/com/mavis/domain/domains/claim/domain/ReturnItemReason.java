package com.mavis.domain.domains.claim.domain;

import com.mavis.domain.domains.order.domain.OrderItem;

public record ReturnItemReason(
        OrderItem orderItem,
        String reason
) {
}
