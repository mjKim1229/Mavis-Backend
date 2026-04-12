package com.mavis.api.review.dto;

import com.mavis.common.util.DateFormatters;
import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.review.domain.Review;
import com.mavis.domain.domains.user.domain.User;
import lombok.Builder;

import java.util.List;

@Builder
public record UserReviewResponse(
        Long reviewId,
        int score,
        String createdAt,
        String color,
        int quantity,
        String content,
        List<String> imageUrls,
        String name
) {
    public static UserReviewResponse of(Review review, User user, OrderItem orderItem, List<String> imageUrls) {
        return UserReviewResponse.builder()
                .reviewId(review.getId())
                .score(review.getScore())
                .createdAt(review.getCreatedAt().format(DateFormatters.DATE_FORMATTER))
                .imageUrls(imageUrls)
                .content(review.getContentForPublic())
                .name(maskName(user.getName()))
                .quantity(orderItem.getQuantity())
                .color(orderItem.getColor())
                .build();
    }

    private static String maskName(String name) {
        if (name == null || name.isEmpty()) return name;
        return name.charAt(0) + "*".repeat(name.length() - 1);
    }
}

