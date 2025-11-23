package com.mavis.api.order.dto;

import com.mavis.domain.domains.order.domain.OrderOption;

public record OrderProduct(
        Long productId,
        OrderOption option
) {
}
