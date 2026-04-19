package com.mavis.domain.domains.review.vo;

import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.product.domain.Product;
import lombok.Builder;

@Builder
public record GetWritableUserOrderItemResponseVO(
        Long id,
        String name,
        Integer price,
        String color,
        int quantity,
        String previewImage
) {
    public static GetWritableUserOrderItemResponseVO from(OrderItem orderItem, Product product, String previewImage) {
        return GetWritableUserOrderItemResponseVO.builder()
                .id(orderItem.getId())
                .name(product.getName())
                .price(orderItem.getPrice())
                .color(orderItem.getColor())
                .quantity(orderItem.getQuantity())
                .previewImage(previewImage)
                .build();
    }
}
