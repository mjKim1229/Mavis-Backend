package com.mavis.api.inquiry.dto;

import com.mavis.domain.domains.inquiry.domain.Inquiry;
import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.user.domain.User;

public record CreateInquiryRequest(
        String question,
        boolean isPrivate
) {
    public Inquiry toEntity(Product product, User user) {
        return Inquiry.builder()
                .question(question)
                .isPrivate(isPrivate)
                .product(product)
                .user(user)
                .build();
    }
}
