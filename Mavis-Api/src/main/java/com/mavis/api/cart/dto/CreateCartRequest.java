package com.mavis.api.cart.dto;

public record CreateCartRequest(
        Long productId,
        String color,
        int quantity
) {
}
