package com.mavis.api.inquiry.dto;

import com.mavis.common.util.DateFormatters;
import com.mavis.domain.domains.inquiry.domain.Inquiry;
import com.mavis.domain.domains.inquiry.dto.ProductInquiryRow;
import com.mavis.domain.domains.user.domain.User;
import lombok.Builder;

@Builder
public record InquiryResponse(
        Long id,
        String question,
        String createdAt,
        String userName
) {
        public static InquiryResponse from(Inquiry inquiry, User user) {
                return InquiryResponse.builder()
                        .id(inquiry.getId())
                        .question(inquiry.getQuestion())
                        .createdAt(inquiry.getCreatedAt().format(DateFormatters.DATE_FORMATTER))
                        .userName(maskName(user.getName()))
                        .build();
        }

        public static InquiryResponse from(ProductInquiryRow dto) {
                return InquiryResponse.builder()
                        .id(dto.inquiryId())
                        .question(dto.question())
                        .createdAt(dto.inquiryCreatedAt().format(DateFormatters.DATE_FORMATTER))
                        .userName(maskName(dto.userName()))
                        .build();
        }

        private static String maskName(String name) {
                if (name == null || name.length() <= 1) return name;
                return name.charAt(0) + "*".repeat(name.length() - 1);
        }
}
