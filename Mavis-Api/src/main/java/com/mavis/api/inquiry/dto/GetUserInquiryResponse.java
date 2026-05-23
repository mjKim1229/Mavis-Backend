package com.mavis.api.inquiry.dto;

import com.mavis.common.util.DateFormatters;
import com.mavis.domain.domains.inquiry.dto.UserInquiryRow;

public record GetUserInquiryResponse(
        Long id,
        Long productId,
        String productName,
        String question,
        String questionCreatedAt,
        String answer,
        String answerCreatedAt
) {
    public static GetUserInquiryResponse from(UserInquiryRow dto) {
        String answerCreatedAt = dto.answerCreatedAt() != null
                ? dto.answerCreatedAt().format(DateFormatters.DATE_FORMATTER)
                : null;
        return new GetUserInquiryResponse(
                dto.inquiryId(),
                dto.productId(),
                dto.productName(),
                dto.question(),
                dto.questionCreatedAt().format(DateFormatters.DATE_FORMATTER),
                dto.answer(),
                answerCreatedAt
        );
    }
}
