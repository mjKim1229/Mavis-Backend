package com.mavis.api.review.controller;

import com.mavis.api.common.page.PageResponse;
import com.mavis.api.review.dto.CreateReviewRequest;
import com.mavis.api.review.dto.GetWritableUserOrderItemResponse;
import com.mavis.api.review.dto.ReviewResponse;
import com.mavis.api.review.dto.UserReviewResponse;
import com.mavis.api.review.service.ReviewService;
import com.mavis.domain.domains.review.vo.ProductReviewTotal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/review")
@Tag(name = "리뷰 API")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "리뷰 생성", description = "사용자가 상품에 대한 리뷰를 작성합니다.")
    @PostMapping("/user")
    public void createReview(@RequestPart CreateReviewRequest request,
                             @RequestPart List<MultipartFile> images) {
        reviewService.createReview(request, images);
    }

    @Operation(summary = "사용자 리뷰 목록 조회", description = "사용자가 작성한 리뷰 목록을 조회합니다.")
    @GetMapping("/user")
    public PageResponse<UserReviewResponse> getUserReviewList(Pageable pageable) {
        return reviewService.getUserReviews(pageable);
    }

    @Operation(summary = "사용자 리뷰 작성 가능 리스트 조회")
    @GetMapping("/user/review-writable")
    public PageResponse<GetWritableUserOrderItemResponse> getWritableUserOrderItemResponsePageResponse(Pageable pageable) {
        return reviewService.getWritableOrderItems(pageable);
    }

    @Operation(summary = "상품 리뷰 통계 조회", description = "특정 상품의 리뷰 통계 정보를 조회합니다.")
    @GetMapping("/product/{productId}/total")
    public ProductReviewTotal getProductReviewTotal(@PathVariable Long productId) {
        return reviewService.getProductReviewTotal(productId);
    }

    @Operation(summary = "상품 리뷰 목록 조회", description = "특정 상품에 작성된 리뷰 목록을 조회합니다.")
    @GetMapping("/product/{productId}")
    public PageResponse<ReviewResponse> getProductReviews(@PathVariable Long productId,
                                                          @RequestParam(defaultValue = "false") boolean photoOnly,
                                                          @ParameterObject @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return reviewService.getProductReviews(productId, photoOnly, pageable);
    }
}
