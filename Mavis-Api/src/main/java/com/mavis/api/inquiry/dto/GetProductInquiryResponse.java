package com.mavis.api.inquiry.dto;

import com.mavis.domain.domains.inquiry.domain.Inquiry;
import com.mavis.domain.domains.inquiry.domain.InquiryAnswer;
import lombok.Builder;

//TODO Admin, User 닉네임
@Builder
public record GetProductInquiryResponse(
        InquiryResponse inquiry,
        InquiryAnswerResponse inquiryAnswer,
        boolean isPrivate
) {
    public static GetProductInquiryResponse from(Inquiry inquiry, InquiryAnswer answer) {
        return GetProductInquiryResponse.builder()
                .isPrivate(inquiry.isPrivate())
                .build();
    }
}
