package com.mavis.api.review.service;

import com.mavis.api.auth.implement.UserReader;
import com.mavis.api.common.page.PageResponse;
import com.mavis.api.review.dto.*;
import com.mavis.api.review.implement.ReviewImageUploader;
import com.mavis.api.review.implement.ReviewValidator;
import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.order.implement.OrderReader;
import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.product.implement.ProductReader;
import com.mavis.domain.domains.review.domain.Review;
import com.mavis.domain.domains.review.domain.ReviewImage;
import com.mavis.domain.domains.review.exception.ReviewNotFoundException;
import com.mavis.domain.domains.review.exception.UnauthorizedReviewException;
import com.mavis.domain.domains.review.repository.ReviewRepository;
import com.mavis.domain.domains.review.vo.GetWritableUserOrderItemResponseVO;
import com.mavis.domain.domains.user.domain.User;
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
    private final UserReader userReader;
    private final ReviewValidator reviewValidator;
    private final ProductReader productReader;

    @Transactional
    public void createReview(CreateReviewRequest request, List<MultipartFile> images) {
        User user = userReader.getCurrentUser();
        OrderItem orderItem = orderReader.findOrderItemById(request.orderItemId());
        reviewValidator.validateOrderUserMatch(user, orderItem.getOrder());
        Review review = request.toEntity(orderItem, user);
        Review savedReview = reviewRepository.save(review);
        reviewImageUploader.saveReviewImages(images, savedReview);
    }

    @Transactional
    public void updateReview(Long reviewId, UpdateReviewRequest request, List<MultipartFile> images) {
        User user = userReader.getCurrentUser();
        Review review = reviewRepository.findByIdAndIsDeletedFalse(reviewId)
            .orElseThrow(() -> ReviewNotFoundException.EXCEPTION);
        if (!review.getUser().getId().equals(user.getId())) {
            throw UnauthorizedReviewException.EXCEPTION;
        }
        review.update(request.content());

        List<ReviewImageVO> keepImages = request.keepImages() != null ? request.keepImages() : List.of();
        reviewImageUploader.updateKeptImages(review, keepImages);

        if (images != null && !images.isEmpty()) {
            int maxOrder = keepImages.stream().mapToInt(ReviewImageVO::order).max().orElse(-1);
            reviewImageUploader.saveReviewImages(images, review, maxOrder + 1);
        }
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        User user = userReader.getCurrentUser();
        Review review = reviewRepository.findByIdAndIsDeletedFalse(reviewId)
            .orElseThrow(() -> ReviewNotFoundException.EXCEPTION);
        if (!review.getUser().getId().equals(user.getId())) {
            throw UnauthorizedReviewException.EXCEPTION;
        }
        review.delete();
    }

    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getProductReviews(Long productId, boolean photoOnly, Pageable pageable) {
        Product product = productReader.readById(productId);
        Page<ReviewResponse> reviewPages = reviewRepository.queryProductReviews(product.getId(), photoOnly, pageable)
            .map(review -> {
                List<String> reviewImages = extractReviewImages(review);
                return ReviewResponse.of(review, review.getUser(), review.getOrderItem(), reviewImages);
            });

        return PageResponse.of(reviewPages);
    }

    @Transactional(readOnly = true)
    public UserReviewResponse getUserReviewById(Long reviewId) {
        User user = userReader.getCurrentUser();
        Review review = reviewRepository.findByIdAndIsDeletedFalse(reviewId)
            .orElseThrow(() -> ReviewNotFoundException.EXCEPTION);
        if (!review.getUser().getId().equals(user.getId())) {
            throw UnauthorizedReviewException.EXCEPTION;
        }
        List<String> imageUrls = extractReviewImages(review);
        return UserReviewResponse.of(review, review.getOrderItem(), imageUrls);
    }

    @Transactional(readOnly = true)
    public PageResponse<UserReviewResponse> getUserReviews(Pageable pageable) {
        User user = userReader.getCurrentUser();
        Page<UserReviewResponse> reviewPages = reviewRepository.queryProductReviewsByUser(user, pageable)
            .map(review -> {
                List<String> imageUrls = extractReviewImages(review);
                return UserReviewResponse.of(review, review.getOrderItem(), imageUrls);
            });
        return PageResponse.of(reviewPages);
    }

    private static List<String> extractReviewImages(Review review) {
        return review.getImages()
            .stream()
            .filter(image -> !image.isDeleted())
            .map(ReviewImage::getImageUrl)
            .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<GetWritableUserOrderItemResponseVO> getWritableOrderItems(Pageable pageable) {
        User user = userReader.getCurrentUser();

        Page<GetWritableUserOrderItemResponseVO> dtoList =
            reviewRepository.queryWritableOrderItemsByUser(user, pageable);

        return PageResponse.of(dtoList);
    }
}
