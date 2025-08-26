package com.mavis.api.inquiry.dto;

import com.mavis.api.inquiry.domain.Inquiry;
import lombok.Builder;

import java.time.LocalDate;

//TODO Admin, User 닉네임
@Builder
public record GetProductInquiryResponse(
        String question,
        LocalDate questionCreatedAt,
        String answer,
        LocalDate answerCreatedAt,
        String userNickname,
        boolean isPrivate
) {
    public static GetProductInquiryResponse from(Inquiry inquiry) {
        return GetProductInquiryResponse.builder()
                .question(inquiry.getQuestion())
                .questionCreatedAt(inquiry.getCreatedAt().toLocalDate())
                .answer(inquiry.getAnswer())
                .answerCreatedAt(inquiry.getAnsweredDatetime().toLocalDate())
                .isPrivate(inquiry.isPrivate())
                .build();
    }
}
