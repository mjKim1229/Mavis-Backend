package com.mavis.api.review.dto;

import com.mavis.api.review.domain.Review;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record ReviewResponse(
        Long reviewId,
        int score,
        LocalDateTime createdAt,
        int quantity,
        List<String> imageUrls
) {
    public static ReviewResponse of(Review review, List<String> imageUrls) {
        return ReviewResponse.builder()
                .reviewId(review.getId())
                .score(review.getScore())
                .createdAt(review.getCreatedAt())
                .imageUrls(imageUrls)
                .build();
    }
}
