package com.mavis.admin.domains.order.service;

import com.mavis.admin.domains.order.dto.OrderInfo;
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
public class AdminOrderService {
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public List<OrderInfo> getOrderLists(Pageable pageable, OrderStatus orderStatus) {
        List<Order> orderLists = orderRepository.findOrderLists(pageable, orderStatus);
        return orderLists.stream()
                .map(order -> {
                            OrderAddress orderAddress = order.getOrderAddress();
                            User user = order.getUser();
                            return OrderInfo.builder()
                                    .address(orderAddress.getAddress())
                                    .addressInfo(orderAddress.getAddressMemo())
                                    .totalPrice(order.getTotalPrice())
                                    .userName(user.getName())
                                    .build();
                        }
                ).toList();
    }
}
