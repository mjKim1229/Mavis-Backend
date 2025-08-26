package com.mavis.api.review.repository;

import com.mavis.api.review.dto.ProductReviewTotal;

public interface ReviewCustomRepository {
    ProductReviewTotal queryProductReviewTotal(Long productId);
}
