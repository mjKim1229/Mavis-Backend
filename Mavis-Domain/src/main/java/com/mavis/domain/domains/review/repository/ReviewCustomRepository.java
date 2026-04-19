package com.mavis.domain.domains.review.repository;

import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.review.domain.Review;
import com.mavis.domain.domains.review.vo.GetWritableUserOrderItemResponseVO;
import com.mavis.domain.domains.review.vo.ProductReviewTotal;
import com.mavis.domain.domains.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewCustomRepository {
    ProductReviewTotal queryProductReviewTotal(Long productId);

    Page<Review> queryProductReviews(Long productId, boolean photoOnly, Pageable pageable);

    Page<Review> queryProductReviewsByUser(User user, Pageable pageable);

    Page<GetWritableUserOrderItemResponseVO> queryWritableOrderItemsByUser(User user, Pageable pageable);
}
