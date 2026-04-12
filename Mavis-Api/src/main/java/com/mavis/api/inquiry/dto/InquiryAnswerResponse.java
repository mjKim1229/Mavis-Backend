package com.mavis.api.inquiry.dto;

import com.mavis.common.util.DateFormatters;
import com.mavis.domain.domains.inquiry.domain.InquiryAnswer;
import lombok.Builder;

@Builder
public record InquiryAnswerResponse(
        String answer,
        String createdAt,
        String adminName
) {
        public static InquiryAnswerResponse from(InquiryAnswer answer) {
                return InquiryAnswerResponse.builder()
                        .answer(answer.getAnswer())
                        .createdAt(answer.getCreatedAt().format(DateFormatters.DATE_FORMATTER))
                        .adminName("관리자")
                        .build();
        }
}
