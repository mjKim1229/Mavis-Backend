package com.mavis.api.cart.dto;

import com.mavis.domain.domains.cart.domain.CartItem;
import com.mavis.domain.domains.product.domain.Product;
import lombok.Builder;

@Builder
public record GetCartResponse(
        Long cartItemId,
        Long productId,
        String productName,
        String productImageUrl,
        String color,
        int quantity,
        int totalPrice
) {
    public static GetCartResponse from(CartItem cartItem, Product product) {
        int totalPrice = product.getPrice() * cartItem.getQuantity();
        return GetCartResponse.builder()
                .cartItemId(cartItem.getId())
                .productId(product.getId())
                .productName(product.getName())
                .productImageUrl(product.getMainImageUrl())
                .color(cartItem.getColor())
                .quantity(cartItem.getQuantity())
                .totalPrice(totalPrice)
                .build();
    }
}
