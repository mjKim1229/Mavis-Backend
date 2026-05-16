package com.mavis.admin.domains.inquiry.dto;

import com.mavis.common.util.DateFormatters;
import com.mavis.domain.domains.inquiry.domain.Inquiry;
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
    public static GetAdminInquiryDetailResponse from(Inquiry inquiry) {
        boolean hasAnswer = inquiry.getInquiryAnswer() != null && !inquiry.getInquiryAnswer().isDeleted();

        return GetAdminInquiryDetailResponse.builder()
                .inquiryId(inquiry.getId())
                .productId(inquiry.getProduct().getId())
                .productName(inquiry.getProduct().getName())
                .productMainImageUrl(inquiry.getProduct().getMainImageUrl())
                .question(inquiry.getQuestion())
                .userName(inquiry.getUser().getName())
                .createdAt(inquiry.getCreatedAt().format(DateFormatters.DATE_FORMATTER))
                .isAnswered(hasAnswer)
                .answer(hasAnswer ? inquiry.getInquiryAnswer().getAnswer() : null)
                .answeredAt(hasAnswer ? inquiry.getInquiryAnswer().getCreatedAt().format(DateFormatters.DATE_FORMATTER) : null)
                .build();
    }
}
