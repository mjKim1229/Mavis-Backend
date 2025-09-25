package com.mavis.api.inquiry.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mavis.domain.domains.admin.domain.Admin;
import com.mavis.domain.domains.inquiry.domain.InquiryAnswer;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record InquiryAnswerResponse(
        String answer,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDateTime createdAt,
        String adminName
) {
        public static InquiryAnswerResponse from(InquiryAnswer answer) {
                return InquiryAnswerResponse.builder()
                        .answer(answer.getAnswer())
                        .createdAt(answer.getCreatedAt())
                        .adminName("관리자")
                        .build();
        }
}
