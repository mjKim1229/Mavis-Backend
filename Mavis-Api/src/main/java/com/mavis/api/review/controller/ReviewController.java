package com.mavis.api.review.controller;

import com.mavis.api.common.page.PageResponse;
import com.mavis.api.review.dto.CreateReviewRequest;
import com.mavis.api.review.dto.ReviewResponse;
import com.mavis.api.review.dto.UserReviewResponse;
import com.mavis.api.review.service.ReviewService;
import com.mavis.domain.domains.review.vo.ProductReviewTotal;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/review")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/user")
    public void createReview(@RequestPart CreateReviewRequest request,
                             @RequestPart List<MultipartFile> images) {
        reviewService.createReview(request, images);
    }

    @GetMapping("/user")
    public PageResponse<UserReviewResponse> getUserReviewList(Pageable pageable) {
        return reviewService.getUserReviews(pageable);
    }

    @GetMapping("/{productId}/total")
    public ProductReviewTotal getProductReviewTotal(@PathVariable Long productId) {
        return reviewService.getProductReviewTotal(productId);
    }

    @GetMapping("/{productId}")
    public PageResponse<ReviewResponse> getProductReviews(@PathVariable Long productId, @ParameterObject Pageable pageable) {
        return reviewService.getProductReviews(productId, pageable);
    }
}
