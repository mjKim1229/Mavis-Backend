package com.mavis.api.inquiry.dto;

import com.mavis.common.util.DateFormatters;
import com.mavis.domain.domains.inquiry.dto.ProductInquiryRow;
import lombok.Builder;

@Builder
public record InquiryAnswerResponse(
        String answer,
        String createdAt,
        String adminName
) {
    public static InquiryAnswerResponse from(ProductInquiryRow dto) {
        return InquiryAnswerResponse.builder()
                .answer(dto.answer())
                .createdAt(dto.answerCreatedAt().format(DateFormatters.DATE_FORMATTER))
                .adminName("관리자")
                .build();
    }
}
