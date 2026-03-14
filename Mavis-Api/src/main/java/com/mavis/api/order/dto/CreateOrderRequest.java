package com.mavis.api.order.dto;

import java.util.List;

public record CreateOrderRequest(
        int amount,
        OrderAddressRequest orderAddressRequest,
        List<OrderProduct> orderItems
) {
}
