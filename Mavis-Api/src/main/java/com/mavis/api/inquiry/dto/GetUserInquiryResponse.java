package com.mavis.api.inquiry.dto;

import com.mavis.api.inquiry.service.UserInquiryAnswerResult;
import com.mavis.api.inquiry.service.UserInquiryResult;
import com.mavis.common.util.DateFormatters;

public record GetUserInquiryResponse(
        Long id,
        Long productId,
        String productName,
        String question,
        String questionCreatedAt,
        String answer,
        String answerCreatedAt
) {
    public static GetUserInquiryResponse from(UserInquiryResult inquiry, UserInquiryAnswerResult answer) {
        String answerCreatedAt = answer.answerCreatedAt() != null
                ? answer.answerCreatedAt().format(DateFormatters.DATE_FORMATTER)
                : null;
        return new GetUserInquiryResponse(
                inquiry.id(),
                inquiry.productId(),
                inquiry.productName(),
                inquiry.question(),
                inquiry.questionCreatedAt().format(DateFormatters.DATE_FORMATTER),
                answer.answer(),
                answerCreatedAt
        );
    }
}
