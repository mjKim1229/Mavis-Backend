package com.mavis.api.order.dto;

import java.util.List;

public record CreateOrderRequest(
        String orderId,
        int amount,
        OrderAddressRequest orderAddressRequest,
        List<OrderProduct> orderItems
) {
}
