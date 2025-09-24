package com.mavis.api.order.service;

import com.mavis.api.auth.implement.UserReader;
import com.mavis.api.order.dto.CreateOrderRequest;
import com.mavis.api.product.implement.ProductReader;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.order.domain.OrderOption;
import com.mavis.domain.domains.order.repository.OrderItemRepository;
import com.mavis.domain.domains.order.repository.OrderRepository;
import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final ProductReader productReader;
    private final UserReader userReader;

    public void createOrder(List<CreateOrderRequest> orderItemRequests) {
        User user = userReader.getCurrentUser();
        Order order = Order.of(user);
        Order savedOrder = orderRepository.save(order);

        List<OrderItem> orderItems = orderItemRequests.stream()
                .map(request -> {
                    Product product = productReader.readById(request.productId());
                    OrderOption option = request.option();
                    return OrderItem.of(option, countPrice(option.quantity(), product.getPrice()));
                })
                .toList();
        orderItemRepository.saveAll(orderItems);
    }

    private int countPrice(int quantity, int price) {
        return quantity * price;
    }
}
