package com.mavis.api.cart.dto;

import com.mavis.domain.domains.cart.domain.CartItem;
import com.mavis.domain.domains.product.domain.Product;
import lombok.Builder;

@Builder
public record GetCartResponse(
        Long cartItemId,
        String productName,
        String productImageUrl,
        String color,
        int quantity,
        int totalPrice
) {
    public static GetCartResponse from(CartItem cartItem, Product product) {
        return GetCartResponse.builder()
                .cartItemId(cartItem.getId())
                .productName(product.getName())
                //TODO 이미지
                .productImageUrl("imageUrl")
                .color(cartItem.getColor())
                .quantity(cartItem.getQuantity())
                .totalPrice(cartItem.getTotalPrice())
                .build();
    }
}
