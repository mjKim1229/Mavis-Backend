package com.mavis.api.inquiry.service;

import java.time.LocalDateTime;

public record UserInquiryResult(
        Long id,
        Long productId,
        String productName,
        String question,
        LocalDateTime questionCreatedAt
) {
}
