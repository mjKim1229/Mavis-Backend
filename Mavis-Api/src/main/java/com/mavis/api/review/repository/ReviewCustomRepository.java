package com.mavis.api.review.repository;

import com.mavis.api.review.dto.ProductReviewTotal;
import com.mavis.api.review.dto.ReviewResponse;

import java.util.List;

public interface ReviewCustomRepository {
    ProductReviewTotal queryProductReviewTotal(Long productId);
    List<ReviewResponse> queryProductReviews(Long productId);
}
