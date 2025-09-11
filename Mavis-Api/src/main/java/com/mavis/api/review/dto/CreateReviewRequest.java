package com.mavis.api.review.dto;

import com.mavis.api.order.domain.Order;
import com.mavis.api.review.domain.Review;

public record CreateReviewRequest(
        int score,
        String content,
        Long orderId
) {
    public Review toEntity(Order order) {
        return Review.builder()
                .score(score)
                .content(content)
                .order(order)
                .build();
    }
}
