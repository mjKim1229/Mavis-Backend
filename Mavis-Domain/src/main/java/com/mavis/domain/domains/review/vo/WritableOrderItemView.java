package com.mavis.domain.domains.review.vo;

public record WritableOrderItemView(
        Long id,
        String productName,
        Integer price,
        String color,
        Integer quantity,
        String previewImage
) {
}
