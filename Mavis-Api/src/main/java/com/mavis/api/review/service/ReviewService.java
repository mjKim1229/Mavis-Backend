package com.mavis.api.review.service;

import com.mavis.api.common.page.PageResponse;
import com.mavis.api.review.domain.Review;
import com.mavis.api.review.domain.ReviewImage;
import com.mavis.api.review.dto.CreateReviewRequest;
import com.mavis.api.review.dto.ProductReviewTotal;
import com.mavis.api.review.dto.ReviewResponse;
import com.mavis.api.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    @Transactional
    public void createReview(CreateReviewRequest request) {
        //TODO order 조회 후 검증 [user]
        Review review = request.toEntity();
        reviewRepository.save(review);
    }

    @Transactional(readOnly = true)
    public ProductReviewTotal getProductReviewTotal(Long productId) {
        return reviewRepository.queryProductReviewTotal(productId);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getProductReviews(Long productId, Pageable pageable) {
        Page<ReviewResponse> reviewPages = reviewRepository.queryProductReviews(productId, pageable)
                .map(review -> {
                    List<String> reviewImages = review.getImages()
                            .stream()
                            .map(ReviewImage::getImageUrl)
                            .toList();
                    return ReviewResponse.of(review, reviewImages);
                });

        return PageResponse.of(reviewPages);
    }
}
