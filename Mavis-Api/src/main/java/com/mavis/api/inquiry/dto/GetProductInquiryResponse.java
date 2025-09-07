package com.mavis.api.inquiry.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mavis.api.inquiry.domain.Inquiry;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

//TODO Admin, User 닉네임
@Builder
public record GetProductInquiryResponse(
        String question,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDateTime questionCreatedAt,
        String answer,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDateTime answerCreatedAt,
        String userNickname,
        boolean isPrivate
) {
    public static GetProductInquiryResponse from(Inquiry inquiry) {
        return GetProductInquiryResponse.builder()
                .question(inquiry.getQuestion())
                .questionCreatedAt(inquiry.getCreatedAt())
                .answer(inquiry.getAnswer())
                .answerCreatedAt(inquiry.getAnsweredDatetime())
                .isPrivate(inquiry.isPrivate())
                .build();
    }
}
