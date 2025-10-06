package com.mavis.api.review.service;

import com.mavis.api.auth.implement.UserReader;
import com.mavis.api.common.page.PageResponse;
import com.mavis.api.product.implement.ProductReader;
import com.mavis.api.review.dto.CreateReviewRequest;
import com.mavis.api.review.dto.ReviewResponse;
import com.mavis.api.review.implement.ReviewImageUploader;
import com.mavis.api.review.implement.ReviewValidator;
import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.order.implement.OrderReader;
import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.review.domain.Review;
import com.mavis.domain.domains.review.domain.ReviewImage;
import com.mavis.domain.domains.review.repository.ReviewRepository;
import com.mavis.domain.domains.review.vo.ProductReviewTotal;
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
        OrderItem orderItem = orderReader.findById(request.orderItemId());
        reviewValidator.validateOrderUserMatch(user, orderItem.getOrder());
        Review review = request.toEntity(orderItem, user);
        Review savedReview = reviewRepository.save(review);
        reviewImageUploader.saveReviewImages(images, savedReview);
    }

    @Transactional(readOnly = true)
    public ProductReviewTotal getProductReviewTotal(Long productId) {
        Product product = productReader.readById(productId);
        return reviewRepository.queryProductReviewTotal(product.getId());
    }

    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getProductReviews(Long productId, Pageable pageable) {
        Product product = productReader.readById(productId);
        Page<ReviewResponse> reviewPages = reviewRepository.queryProductReviews(product.getId(), pageable)
                .map(review -> {
                    List<String> reviewImages = review.getImages()
                            .stream()
                            .map(ReviewImage::getImageUrl)
                            .toList();
                    return ReviewResponse.of(review, review.getUser(), review.getOrderItem(), reviewImages);
                });

        return PageResponse.of(reviewPages);
    }
}
