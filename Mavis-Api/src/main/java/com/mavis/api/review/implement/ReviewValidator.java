package com.mavis.api.review.implement;

import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.review.exception.ReviewOrderUserNotMatchException;
import com.mavis.domain.domains.user.domain.User;
import org.springframework.stereotype.Component;

@Component
public class ReviewValidator {

    public void validateOrderUserMatch(User user, Order order) {
        if (!order.getUser().getId().equals(user.getId())) {
            throw ReviewOrderUserNotMatchException.Exception;
        }
    }
}
