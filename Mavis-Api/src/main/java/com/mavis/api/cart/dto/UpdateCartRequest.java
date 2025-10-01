package com.mavis.api.cart.dto;

public record UpdateCartRequest(
        String color,
        int quantity
) {
}
