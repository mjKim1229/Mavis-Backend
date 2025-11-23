package com.mavis.api.review.dto;

import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.review.domain.Review;
import com.mavis.domain.domains.user.domain.User;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record UserReviewResponse(
        Long reviewId,
        int score,
        LocalDateTime createdAt,
        String color,
        int quantity,
        String content,
        List<String> imageUrls,
        String username
) {
    public static UserReviewResponse of(Review review, User user, OrderItem orderItem, List<String> imageUrls) {
        return UserReviewResponse.builder()
                .reviewId(review.getId())
                .score(review.getScore())
                .createdAt(review.getCreatedAt())
                .imageUrls(imageUrls)
                .content(review.getContentForPublic())
                .username(user.getName())
                .quantity(orderItem.getQuantity())
                .color(orderItem.getColor())
                .build();
    }
}

