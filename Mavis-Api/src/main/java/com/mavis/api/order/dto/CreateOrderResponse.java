package com.mavis.api.order.dto;

public record CreateOrderResponse(
        String orderId
) {
    public static CreateOrderResponse from(String orderId) {
        return new CreateOrderResponse(orderId);
    }
}
