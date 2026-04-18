package com.mavis.api.review.dto;

import com.mavis.domain.domains.review.vo.WritableOrderItemView;
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
    public static GetWritableUserOrderItemResponse from(WritableOrderItemView view) {
        return GetWritableUserOrderItemResponse.builder()
                .id(view.id())
                .name(view.productName())
                .price(view.price())
                .color(view.color())
                .quantity(view.quantity())
                .previewImage(view.previewImage())
                .build();
    }
}
