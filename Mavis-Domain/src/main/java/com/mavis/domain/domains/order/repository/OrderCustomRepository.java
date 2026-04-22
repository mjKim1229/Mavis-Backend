package com.mavis.domain.domains.order.repository;

import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderCustomRepository {
    Page<Order> findPaymentConfirmedOrderPages(Pageable pageable);

    Page<Order> findOrderedOrderPages(Pageable pageable);

    Page<Order> findOrderPagesByUser(Pageable pageable, User user);

    Page<OrderItem> findUserOrderItemCanReview(Pageable pageable, User user);

    long countOrderedWithReadyDelivery();
}
