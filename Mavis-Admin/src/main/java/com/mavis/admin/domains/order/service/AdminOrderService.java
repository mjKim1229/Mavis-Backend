package com.mavis.admin.domains.order.service;

import com.mavis.admin.common.page.PageResponse;
import com.mavis.admin.domains.order.dto.AdminOrderConfirmRequest;
import com.mavis.admin.domains.order.dto.AdminOrderCountResponse;
import com.mavis.admin.domains.order.dto.GetAdminOrderResponse;
import com.mavis.admin.domains.order.dto.OrderItemInfo;
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

import java.time.LocalDate;
import java.util.List;

import static com.mavis.admin.domains.delivery.service.AdminDeliveryService.EXCEL_DATE_FORMATTER;

@Service
@RequiredArgsConstructor
public class AdminOrderService {
    private final OrderRepository orderRepository;
    private final DeliveryRepository deliveryRepository;

    @Transactional(readOnly = true)
    public PageResponse<GetAdminOrderResponse> getPaymentConfirmedOrderLists(Pageable pageable) {
        Page<Order> orderPages = orderRepository.findPaymentConfirmedOrderPages(pageable);
        return PageResponse.of(orderPages.map(this::toOrderResponse));
    }

    @Transactional(readOnly = true)
    public PageResponse<GetAdminOrderResponse> getOrderedOrderLists(Pageable pageable, LocalDate startDate, LocalDate endDate) {
        Page<Order> orderPages = orderRepository.findOrderedOrderPages(pageable, startDate, endDate);
        return PageResponse.of(orderPages.map(this::toOrderResponse));
    }

    private GetAdminOrderResponse toOrderResponse(Order order) {
        OrderAddress orderAddress = order.getOrderAddress();
        User user = order.getUser();
        List<OrderItemInfo> orderItemInfoList = order.getOrderItems().stream().map(
                orderItem -> OrderItemInfo.builder()
                        .productName(orderItem.getProduct().getName())
                        .color(orderItem.getColor())
                        .quantity(orderItem.getQuantity())
                        .build()
        ).toList();
        String orderedAt = order.getCreatedAt().format(EXCEL_DATE_FORMATTER);
        return GetAdminOrderResponse.from(order, orderAddress, orderedAt, orderItemInfoList, user);
    }

    @Transactional(readOnly = true)
    public AdminOrderCountResponse getOrderCounts() {
        long paymentConfirmedCount = orderRepository.countByOrderStatusAndIsDeletedFalse(OrderStatus.PAYMENT_CONFIRMED);
        long orderedCount = orderRepository.countByOrderStatusAndIsDeletedFalse(OrderStatus.ORDERED);
        long shippedCount = deliveryRepository.countByDeliveryStatus(DeliveryStatus.SHIPPED);
        long deliveredCount = deliveryRepository.countByDeliveryStatus(DeliveryStatus.DELIVERED);
        return AdminOrderCountResponse.of(paymentConfirmedCount, orderedCount, shippedCount, deliveredCount);
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