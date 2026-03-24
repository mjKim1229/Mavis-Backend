package com.mavis.api.order.dto;

public record CreateOrderResponse(
        String tossOrderId
) {
    public static CreateOrderResponse from(String tossOrderId) {
        return new CreateOrderResponse(tossOrderId);
    }
}
