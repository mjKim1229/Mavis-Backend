package com.mavis.domain.domains.order.repository;

import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderStatus;
import com.mavis.domain.domains.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderCustomRepository {
    Page<Order> findOrderPages(Pageable pageable, OrderStatus orderStatus);

    Page<Order> findOrderPagesByUser(Pageable pageable, User user);
}
