package com.mavis.api.order.dto;

public record DeliveryFeeResponse(
        int deliveryFee
) {
    public static DeliveryFeeResponse of(int deliveryFee) {
        return new DeliveryFeeResponse(deliveryFee);
    }
}
