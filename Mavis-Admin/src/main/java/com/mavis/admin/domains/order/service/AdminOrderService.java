package com.mavis.admin.domains.order.service;

import com.mavis.admin.common.page.PageResponse;
import com.mavis.admin.domains.order.dto.*;
import com.mavis.domain.domains.delivery.domain.Delivery;
import com.mavis.domain.domains.delivery.domain.DeliveryStatus;
import com.mavis.domain.domains.delivery.repository.DeliveryRepository;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderAddress;
import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.order.domain.OrderStatus;
import com.mavis.domain.domains.order.repository.OrderRepository;
import com.mavis.domain.domains.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminOrderService {
    private final OrderRepository orderRepository;
    private final DeliveryRepository deliveryRepository;

    @Transactional(readOnly = true)
    public PageResponse<GetAdminOrderResponse> getOrderLists(Pageable pageable, OrderStatus orderStatus) {
        Page<Order> orderPages = orderRepository.findOrderPages(pageable, orderStatus);
        Page<GetAdminOrderResponse> orderInfoPages = orderPages.map(order -> {
                    OrderAddress orderAddress = order.getOrderAddress();
                    User user = order.getUser();
                    List<OrderItem> orderItems = order.getOrderItems();
                    List<OrderItemInfo> orderItemInfoList = orderItems.stream().map(
                            orderItem -> OrderItemInfo.builder()
                                    .productName(orderItem.getProduct().getName())
                                    .color(orderItem.getColor())
                                    .quantity(orderItem.getQuantity())
                                    .build()
                    ).toList();
                    return GetAdminOrderResponse.from(order, orderAddress, orderItemInfoList, user);
                }
        );
        return PageResponse.of(orderInfoPages);
    }

    @Transactional
    public void confirmOrder(AdminOrderConfirmRequest request) {
        List<Order> orders = orderRepository.findByIdInAndIsDeletedFalse(request.orderIds());
        orders.forEach(order -> {
            order.confirmOrder();
            Delivery delivery = Delivery.builder()
                    .order(order)
                    .deliveryStatus(DeliveryStatus.READY)
                    .build();
            deliveryRepository.save(delivery);
        });
    }
}