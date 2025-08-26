package com.mavis.api.review.service;

import com.mavis.api.order.repository.OrderRepository;
import com.mavis.api.review.domain.Review;
import com.mavis.api.review.dto.CreateReviewRequest;
import com.mavis.api.review.dto.ProductReviewTotal;
import com.mavis.api.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public void createReview(CreateReviewRequest request) {
        //TODO order 조회 후 검증 [user]
        Review review = request.toEntity();
        reviewRepository.save(review);
    }

    public ProductReviewTotal getProductReviewTotal(Long productId) {
        return reviewRepository.queryProductReviewTotal(productId);
    }
}
