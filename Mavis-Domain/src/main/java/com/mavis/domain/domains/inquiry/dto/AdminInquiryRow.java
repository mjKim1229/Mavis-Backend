package com.mavis.domain.domains.inquiry.dto;

import java.time.LocalDateTime;

public record AdminInquiryRow(
        Long inquiryId,
        Long productId,
        String productName,
        String question,
        String userName,
        LocalDateTime createdAt,
        String answer
) {
}
