package com.mavis.api.review.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ReviewResponse(
        Long reviewId,
        int score,
        LocalDateTime createdAt,
        int quantity,
        List<String> imageUrls
) {
}
