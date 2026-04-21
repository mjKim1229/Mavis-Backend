package com.mavis.api.inquiry.dto;

public record GetUserInquiryResponse(
        Long productId,
        String productName,
        String question,
        String questionCreatedAt,
        String answer,
        String answerCreatedAt
) {
}
