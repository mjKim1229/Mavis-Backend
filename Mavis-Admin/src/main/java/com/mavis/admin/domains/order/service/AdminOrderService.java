package com.mavis.admin.domains.order.service;

import com.mavis.admin.common.page.PageResponse;
import com.mavis.admin.domains.order.dto.OrderInfo;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderAddress;
import com.mavis.domain.domains.order.domain.OrderStatus;
import com.mavis.domain.domains.order.repository.OrderRepository;
import com.mavis.domain.domains.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminOrderService {
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public PageResponse<OrderInfo> getOrderLists(Pageable pageable, OrderStatus orderStatus) {
        Page<Order> orderPages = orderRepository.findOrderPages(pageable, orderStatus);
        Page<OrderInfo> orderInfoPages = orderPages.map(order -> {
                    OrderAddress orderAddress = order.getOrderAddress();
                    User user = order.getUser();
                    return OrderInfo.builder()
                            .address(orderAddress.getAddress())
                            .addressInfo(orderAddress.getAddressMemo())
                            .totalPrice(order.getTotalPrice())
                            .userName(user.getName())
                            .build();
                }
        );
        return PageResponse.of(orderInfoPages);
    }
}
