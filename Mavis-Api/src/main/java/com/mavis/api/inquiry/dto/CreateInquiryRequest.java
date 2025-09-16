package com.mavis.api.inquiry.dto;

public record CreateInquiryRequest(
        Long productId,
        String question,
        boolean isPrivate
) {
}
