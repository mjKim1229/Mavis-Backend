package com.mavis.api.order.dto;

import com.mavis.domain.domains.order.domain.OrderOption;

public record CreateOrderRequest(
        Long productId,
        OrderOption option
) {
}
