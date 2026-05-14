package com.mavis.api.inquiry.dto;

public record GetUserInquiryResponse(
        Long id,
        Long productId,
        String productName,
        String question,
        String questionCreatedAt,
        String answer,
        String answerCreatedAt
) {
}
