package com.mavis.domain.domains.inquiry.dto;

import java.time.LocalDateTime;

public record ProductInquiryRow(
        Long inquiryId,
        String question,
        boolean isPrivate,
        LocalDateTime inquiryCreatedAt,
        String userName,
        String answer,
        LocalDateTime answerCreatedAt
) {
}
