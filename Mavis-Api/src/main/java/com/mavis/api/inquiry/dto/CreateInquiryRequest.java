package com.mavis.api.inquiry.dto;

import com.mavis.domain.domains.inquiry.domain.Inquiry;
import com.mavis.domain.domains.product.domain.Product;

public record CreateInquiryRequest(
        Long productId,
        String question,
        boolean isPrivate
) {
    public Inquiry toEntity(Product product, Long userId) {
        return Inquiry.builder()
                .product(product)
                .question(question)
                .isPrivate(isPrivate)
                .questionUserId(userId)
                .build();
    }
}
