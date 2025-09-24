package com.mavis.api.review.service;

import com.mavis.api.common.page.PageResponse;
import com.mavis.api.global.security.SecurityUtils;
import com.mavis.api.review.implement.ReviewImageUploader;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.exception.OrderNotFoundException;
import com.mavis.domain.domains.order.implement.OrderReader;
import com.mavis.domain.domains.order.repository.OrderRepository;
import com.mavis.domain.domains.review.domain.Review;
import com.mavis.domain.domains.review.domain.ReviewImage;
import com.mavis.api.review.dto.CreateReviewRequest;
import com.mavis.domain.domains.review.vo.ProductReviewTotal;
import com.mavis.api.review.dto.ReviewResponse;
import com.mavis.domain.domains.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewImageUploader reviewImageUploader;
    private final OrderReader orderReader;

    @Transactional
    public void createReview(CreateReviewRequest request, List<MultipartFile> images) {
        Long userId = SecurityUtils.getCurrentUserId();
        Order order = orderReader.findById(request.orderId());
        Review review = request.toEntity(order, userId);
        Review savedReview = reviewRepository.save(review);
        reviewImageUploader.saveReviewImages(images, savedReview);
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
