package com.mavis.domain.domains.order.repository;

import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderCustomRepository {
    List<Order> findOrderLists(Pageable pageable, OrderStatus orderStatus);
}
