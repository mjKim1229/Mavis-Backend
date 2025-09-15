package com.mavis.api.review.controller;

import com.mavis.api.common.page.PageResponse;
import com.mavis.api.review.dto.CreateReviewRequest;
import com.mavis.api.review.dto.ProductReviewTotal;
import com.mavis.api.review.dto.ReviewResponse;
import com.mavis.api.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/review")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    public void createReview(@RequestPart CreateReviewRequest request,
                             @RequestPart List<MultipartFile> images) {
        reviewService.createReview(request);
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
