package com.mavis.api.inquiry.dto;

import com.mavis.domain.domains.inquiry.domain.AnswerStatus;
import lombok.Builder;

@Builder
public record GetProductInquiryResponse(
        InquiryResponse inquiry,
        InquiryAnswerResponse inquiryAnswer,
        boolean isPrivate,
        AnswerStatus answerStatus
) {
}
