package com.mavis.api.review.dto;

import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.product.domain.Product;
import lombok.Builder;

@Builder
public record GetWritableUserOrderItemResponse(
        Long id,
        String name,
        Integer price,
        String color,
        int quantity,
        String previewImage
) {
    public static GetWritableUserOrderItemResponse from(OrderItem orderItem, Product product, String previewImage) {
        return GetWritableUserOrderItemResponse.builder()
                .id(orderItem.getId())
                .name(product.getName())
                .price(orderItem.getPrice())
                .color(orderItem.getColor())
                .quantity(orderItem.getQuantity())
                .previewImage(previewImage)
                .build();
    }
}
