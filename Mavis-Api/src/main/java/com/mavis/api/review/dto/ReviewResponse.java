package com.mavis.api.review.dto;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long reviewId,
        int score,
        LocalDateTime createdAt,
        int quantity
) {
}
