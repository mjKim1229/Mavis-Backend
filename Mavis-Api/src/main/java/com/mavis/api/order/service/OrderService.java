package com.mavis.api.order.service;

import com.mavis.api.auth.implement.UserReader;
import com.mavis.api.order.dto.CreateOrderRequest;
import com.mavis.api.order.dto.OrderAddressRequest;
import com.mavis.api.order.dto.UserOrderInfo;
import com.mavis.api.order.implement.OrderItemAppender;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderAddress;
import com.mavis.domain.domains.order.domain.OrderStatus;
import com.mavis.domain.domains.order.repository.OrderRepository;
import com.mavis.domain.domains.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserReader userReader;
    private final OrderItemAppender orderItemAppender;

    @Transactional
    public void createOrder(CreateOrderRequest request) {
        User user = userReader.getCurrentUser();
        OrderAddressRequest orderAddressRequest = request.orderAddressRequest();
        Order order = Order.builder()
                .user(user)
                .orderAddress(orderAddressRequest.toOrderAddress())
                .build();
        Order savedOrder = orderRepository.save(order);
        int totalPrice = orderItemAppender.saveOrderItems(request.orderItems(), savedOrder);
        order.setTotalPrice(totalPrice);
    }

    @Transactional(readOnly = true)
    public List<UserOrderInfo> getUserOrderList(Pageable pageable, OrderStatus orderStatus) {
        User user = userReader.getCurrentUser();
        List<Order> orderLists = orderRepository.findOrderListByUser(pageable, orderStatus, user);
        return orderLists.stream()
                .map(order -> {
                            OrderAddress orderAddress = order.getOrderAddress();
                            return UserOrderInfo.builder()
                                    .address(orderAddress.getAddress())
                                    .addressInfo(orderAddress.getAddressMemo())
                                    .totalPrice(order.getTotalPrice())
                                    .userName(user.getName())
                                    .build();
                        }
                ).toList();
    }
}
