package com.mavis.api.review.implement;

import com.mavis.domain.domains.delivery.domain.DeliveryStatus;
import com.mavis.domain.domains.delivery.repository.DeliveryRepository;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.review.exception.AlreadyReviewedException;
import com.mavis.domain.domains.review.exception.ReviewNotWritableException;
import com.mavis.domain.domains.review.exception.ReviewOrderUserNotMatchException;
import com.mavis.domain.domains.review.repository.ReviewRepository;
import com.mavis.domain.domains.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewValidator {

    private final DeliveryRepository deliveryRepository;
    private final ReviewRepository reviewRepository;

    public void validateOrderUserMatch(User user, Order order) {
        if (!order.getUser().getId().equals(user.getId())) {
            throw ReviewOrderUserNotMatchException.Exception;
        }
    }

    public void validateWritable(OrderItem orderItem) {
        Order order = orderItem.getOrder();
        if (!deliveryRepository.existsByOrderAndDeliveryStatus(order, DeliveryStatus.DELIVERED)) {
            throw ReviewNotWritableException.Exception;
        }
        if (reviewRepository.existsByOrderItem(orderItem)) {
            throw AlreadyReviewedException.Exception;
        }
    }
}
