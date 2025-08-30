package com.mavis.api.review.controller;

import com.mavis.api.common.page.PageResponse;
import com.mavis.api.review.dto.CreateReviewRequest;
import com.mavis.api.review.dto.ProductReviewTotal;
import com.mavis.api.review.dto.ReviewResponse;
import com.mavis.api.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/review")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public void createReview(@RequestBody CreateReviewRequest request) {
        reviewService.createReview(request);
    }

    @GetMapping("/{productId}/total")
    public ProductReviewTotal getProductReviewTotal(@PathVariable Long productId) {
        return reviewService.getProductReviewTotal(productId);
    }

    @GetMapping("/{productId}")
    public PageResponse<ReviewResponse> getProductReviews(@PathVariable Long productId, Pageable pageable) {
        return reviewService.getProductReviews(productId, pageable);
    }
}
