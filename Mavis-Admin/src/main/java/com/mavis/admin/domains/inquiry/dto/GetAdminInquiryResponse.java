package com.mavis.admin.domains.inquiry.dto;

import com.mavis.common.util.DateFormatters;
import com.mavis.domain.domains.inquiry.domain.Inquiry;
import lombok.Builder;

@Builder
public record GetAdminInquiryResponse(
        Long inquiryId,
        Long productId,
        String productName,
        String question,
        String userName,
        String createdAt,
        boolean isAnswered
) {
    public static GetAdminInquiryResponse from(Inquiry inquiry) {
        boolean isAnswered = inquiry.getInquiryAnswer() != null && !inquiry.getInquiryAnswer().isDeleted();

        return GetAdminInquiryResponse.builder()
                .inquiryId(inquiry.getId())
                .productId(inquiry.getProduct().getId())
                .productName(inquiry.getProduct().getName())
                .question(inquiry.getQuestion())
                .userName(inquiry.getUser().getName())
                .createdAt(inquiry.getCreatedAt().format(DateFormatters.DATE_FORMATTER))
                .isAnswered(isAnswered)
                .build();
    }
}
