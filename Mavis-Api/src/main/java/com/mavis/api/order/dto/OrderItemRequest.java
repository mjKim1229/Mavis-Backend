package com.mavis.api.order.dto;

import com.mavis.domain.domains.order.domain.OrderOption;

public record OrderItemRequest(
        Long productId,
        OrderOption option
) {
}
