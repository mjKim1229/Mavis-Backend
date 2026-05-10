package com.mavis.admin.domains.inquiry.dto;

import com.mavis.common.util.DateFormatters;
import com.mavis.domain.domains.inquiry.domain.Inquiry;
import com.mavis.domain.domains.inquiry.domain.InquiryImage;
import lombok.Builder;

import java.util.List;

@Builder
public record GetAdminInquiryDetailResponse(
        Long inquiryId,
        Long productId,
        String productName,
        String productMainImageUrl,
        String question,
        List<String> inquiryImageUrls,
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
                .inquiryImageUrls(inquiry.getInquiryImages().stream()
                        .map(InquiryImage::getImageUrl)
                        .toList())
                .userName(inquiry.getUser().getName())
                .createdAt(inquiry.getCreatedAt().format(DateFormatters.DATE_FORMATTER))
                .isAnswered(hasAnswer)
                .answer(hasAnswer ? inquiry.getInquiryAnswer().getAnswer() : null)
                .answeredAt(hasAnswer ? inquiry.getInquiryAnswer().getCreatedAt().format(DateFormatters.DATE_FORMATTER) : null)
                .build();
    }
}
