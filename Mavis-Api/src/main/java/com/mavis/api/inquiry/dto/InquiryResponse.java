package com.mavis.api.inquiry.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mavis.domain.domains.inquiry.domain.Inquiry;
import com.mavis.domain.domains.user.domain.User;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record InquiryResponse(
        String question,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDateTime createdAt,
        String userName
) {
        public static InquiryResponse from(Inquiry inquiry, User user) {
                return InquiryResponse.builder()
                        .question(inquiry.getQuestion())
                        .createdAt(inquiry.getCreatedAt())
                        .userName(user.getName())
                        .build();
        }
}
