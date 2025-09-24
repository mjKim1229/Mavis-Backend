package com.mavis.api.review.dto;

import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.review.domain.Review;
import com.mavis.domain.domains.user.domain.User;

public record CreateReviewRequest(
        int score,
        String content,
        Long orderId
) {
    public Review toEntity(Order order, User user) {
        return Review.builder()
                .user(user)
                .score(score)
                .content(content)
                .order(order)
                .build();
    }
}
