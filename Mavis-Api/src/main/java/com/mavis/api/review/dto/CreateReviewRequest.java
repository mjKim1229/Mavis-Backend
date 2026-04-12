package com.mavis.api.review.dto;

import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.review.domain.Review;
import com.mavis.domain.domains.user.domain.User;

public record CreateReviewRequest(
        int score,
        String content,
        Long orderItemId,
        boolean isPrivate
) {
    public Review toEntity(OrderItem orderItem, User user) {
        return Review.builder()
                .user(user)
                .score(score)
                .content(content)
                .orderItem(orderItem)
                .isPrivate(isPrivate)
                .build();
    }
}
