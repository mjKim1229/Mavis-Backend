package com.mavis.api.review.dto;

import com.mavis.api.order.dto.OrderItemInfo;
import com.mavis.common.util.DateFormatters;
import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.review.domain.Review;
import lombok.Builder;

import java.util.List;

@Builder
public record UserReviewResponse(
        Long reviewId,
        int score,
        String createdAt,
        String content,
        List<String> imageUrls,
        OrderItemInfo orderItemInfo
) {
    public static UserReviewResponse of(Review review, OrderItem orderItem, List<String> imageUrls) {
        return UserReviewResponse.builder()
                .reviewId(review.getId())
                .score(review.getScore())
                .createdAt(review.getCreatedAt().format(DateFormatters.DATE_FORMATTER))
                .imageUrls(imageUrls)
                .content(review.getContentForPublic())
                .orderItemInfo(OrderItemInfo.from(orderItem))
                .build();
    }
}

