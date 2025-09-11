package com.mavis.api.review.repository;

import com.mavis.api.review.domain.Review;
import com.mavis.api.review.dto.ProductReviewTotal;
import com.mavis.api.review.dto.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewCustomRepository {
    ProductReviewTotal queryProductReviewTotal(Long productId);

    Page<Review> queryProductReviews(Long productId, Pageable pageable);
}
