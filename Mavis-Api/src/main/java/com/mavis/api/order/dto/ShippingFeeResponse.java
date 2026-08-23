package com.mavis.api.order.dto;

public record ShippingFeeResponse(
        int deliveryFee
) {
    public static ShippingFeeResponse of(int deliveryFee) {
        return new ShippingFeeResponse(deliveryFee);
    }
}
