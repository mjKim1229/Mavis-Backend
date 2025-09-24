package com.mavis.api.order.service;

import com.mavis.api.auth.implement.UserReader;
import com.mavis.api.order.dto.CreateOrderRequest;
import com.mavis.api.order.implement.OrderItemAppender;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.repository.OrderRepository;
import com.mavis.domain.domains.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserReader userReader;
    private final OrderItemAppender orderItemAppender;

    public void createOrder(List<CreateOrderRequest> orderItemRequests) {
        User user = userReader.getCurrentUser();
        Order order = Order.of(user);
        Order savedOrder = orderRepository.save(order);
        orderItemAppender.saveOrderItems(orderItemRequests, savedOrder);
    }
}
