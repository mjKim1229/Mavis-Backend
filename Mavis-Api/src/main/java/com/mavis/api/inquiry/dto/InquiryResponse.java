package com.mavis.api.inquiry.dto;

import com.mavis.common.util.DateFormatters;
import com.mavis.domain.domains.inquiry.domain.Inquiry;
import com.mavis.domain.domains.user.domain.User;
import lombok.Builder;

@Builder
public record InquiryResponse(
        String question,
        String createdAt,
        String userName
) {
        public static InquiryResponse from(Inquiry inquiry, User user) {
                return InquiryResponse.builder()
                        .question(inquiry.getQuestion())
                        .createdAt(inquiry.getCreatedAt().format(DateFormatters.DATE_FORMATTER))
                        .userName(user.getName())
                        .build();
        }
}
