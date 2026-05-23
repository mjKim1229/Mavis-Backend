package com.mavis.admin.domains.inquiry.dto;

import com.mavis.common.util.DateFormatters;
import com.mavis.domain.domains.inquiry.domain.Inquiry;
import com.mavis.domain.domains.inquiry.domain.InquiryAnswer;
import lombok.Builder;

@Builder
public record GetAdminInquiryDetailResponse(
        Long inquiryId,
        Long productId,
        String productName,
        String productMainImageUrl,
        String question,
        String userName,
        String createdAt,
        boolean isAnswered,
        String answer,
        String answeredAt
) {
    public static GetAdminInquiryDetailResponse from(Inquiry inquiry, InquiryAnswer answer) {
        boolean hasAnswer = answer != null;

        return GetAdminInquiryDetailResponse.builder()
                .inquiryId(inquiry.getId())
                .productId(inquiry.getProduct().getId())
                .productName(inquiry.getProduct().getName())
                .productMainImageUrl(inquiry.getProduct().getMainImageUrl())
                .question(inquiry.getQuestion())
                .userName(inquiry.getUser().getName())
                .createdAt(inquiry.getCreatedAt().format(DateFormatters.DATE_FORMATTER))
                .isAnswered(hasAnswer)
                .answer(hasAnswer ? answer.getAnswer() : null)
                .answeredAt(hasAnswer ? answer.getCreatedAt().format(DateFormatters.DATE_FORMATTER) : null)
                .build();
    }
}
