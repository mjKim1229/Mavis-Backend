package com.mavis.admin.domains.product.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mavis.common.enums.ProductSubCategory;
import com.mavis.domain.domains.product.domain.Product;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record GetProductPreviewResponse(
        Long id,
        String name,
        Integer price,
        boolean isClearance,
        ProductSubCategory subCategory,
        List<String> colors,
        String previewImage,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDateTime createdAt
) {
    public static GetProductPreviewResponse from(Product product, List<String> colors, String previewImage) {
        return GetProductPreviewResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .isClearance(product.isClearance())
                .subCategory(product.getSubCategory())
                .colors(colors)
                .previewImage(previewImage)
                .createdAt(product.getCreatedAt())
                .build();
    }
}
