package com.mavis.domain.domains.inquiry.dto;

import java.time.LocalDateTime;

public record UserInquiryRow(
        Long inquiryId,
        Long productId,
        String productName,
        String question,
        LocalDateTime questionCreatedAt,
        String answer,
        LocalDateTime answerCreatedAt
) {
}
