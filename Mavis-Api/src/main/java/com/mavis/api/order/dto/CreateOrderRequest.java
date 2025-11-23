package com.mavis.api.order.dto;

import java.util.List;

public record CreateOrderRequest(
        OrderAddressRequest orderAddressRequest,
        List<OrderProduct> orderItems
) {
}
