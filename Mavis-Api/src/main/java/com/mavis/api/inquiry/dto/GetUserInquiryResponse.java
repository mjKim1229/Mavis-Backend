package com.mavis.api.inquiry.dto;

public record GetUserInquiryResponse(
        String question,
        String questionCreatedAt,
        String answer,
        String answerCreatedAt
) {
}
