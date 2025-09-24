package com.mavis.domain.domains.review.repository;

import com.mavis.domain.domains.review.domain.Review;;
import com.mavis.domain.domains.review.vo.ProductReviewTotal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewCustomRepository {
    ProductReviewTotal queryProductReviewTotal(Long productId);

    Page<Review> queryProductReviews(Long productId, Pageable pageable);
}
