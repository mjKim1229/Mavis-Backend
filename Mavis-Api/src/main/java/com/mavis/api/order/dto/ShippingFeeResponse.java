package com.mavis.api.order.dto;

public record ShippingFeeResponse(
        int shippingFee
) {
    public static ShippingFeeResponse of(int shippingFee) {
        return new ShippingFeeResponse(shippingFee);
    }
}
