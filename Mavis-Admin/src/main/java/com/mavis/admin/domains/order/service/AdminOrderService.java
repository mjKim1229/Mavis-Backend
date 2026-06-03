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
import com.mavis.domain.domains.order.domain.OrderStatus;
import com.mavis.domain.domains.order.dto.AdminOrderItemRow;
import com.mavis.domain.domains.order.dto.AdminOrderRow;
import com.mavis.domain.domains.order.exception.OrderNotFoundException;
import com.mavis.domain.domains.order.repository.OrderRepository;
import com.mavis.domain.domains.refund.domain.RefundStatus;
import com.mavis.domain.domains.refund.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminOrderService {
    private final OrderRepository orderRepository;
    private final DeliveryRepository deliveryRepository;
    private final RefundRepository refundRepository;

    @Transactional(readOnly = true)
    public PageResponse<GetAdminOrderResponse> getPaymentConfirmedOrderLists(Pageable pageable) {
        Page<AdminOrderRow> orderRows = orderRepository.findPaymentConfirmedOrderRows(pageable);
        List<Long> orderIds = orderRows.map(AdminOrderRow::orderId).toList();
        List<AdminOrderItemRow> itemRows = orderRepository.findOrderItemRowsByOrderIds(orderIds);
        return buildOrderPageResponse(orderRows, itemRows);
    }

    @Transactional(readOnly = true)
    public PageResponse<GetAdminOrderResponse> getOrderedOrderLists(Pageable pageable) {
        Page<AdminOrderRow> orderRows = orderRepository.findOrderedOrderRows(pageable);
        List<Long> orderIds = orderRows.map(AdminOrderRow::orderId).toList();

        List<AdminOrderItemRow> itemRows = orderRepository.findOrderItemRowsByOrderIds(orderIds);
        return buildOrderPageResponse(orderRows, itemRows);
    }

    private PageResponse<GetAdminOrderResponse> buildOrderPageResponse(Page<AdminOrderRow> orderRows, List<AdminOrderItemRow> itemRows) {
        Map<Long, List<AdminOrderItemRow>> itemsByOrderId = itemRows.stream()
                .collect(Collectors.groupingBy(AdminOrderItemRow::orderId));

        Page<GetAdminOrderResponse> responses = orderRows.map(row -> {
            List<OrderItemInfo> orderItemInfos = itemsByOrderId.getOrDefault(row.orderId(), List.of())
                    .stream()
                    .map(OrderItemInfo::from)
                    .toList();
            return GetAdminOrderResponse.from(row, orderItemInfos);
        });
        return PageResponse.of(responses);
    }

    @Transactional(readOnly = true)
    public AdminOrderCountResponse getOrderCounts() {
        long paymentConfirmedCount = orderRepository.countByOrderStatusAndIsDeletedFalse(OrderStatus.PAYMENT_CONFIRMED);
        long orderedCount = orderRepository.countOrderedWithReadyDelivery();
        long shippedCount = deliveryRepository.countByDeliveryStatus(DeliveryStatus.SHIPPED);
        long deliveredCount = deliveryRepository.countByDeliveryStatus(DeliveryStatus.DELIVERED);
        long refundRequestedCount = refundRepository.countByRefundStatus(RefundStatus.REQUESTED);
        return AdminOrderCountResponse.of(paymentConfirmedCount, orderedCount, shippedCount, deliveredCount, refundRequestedCount);
    }

    @Transactional
    public void confirmOrder(AdminOrderConfirmRequest request) {
        List<Order> orders = orderRepository.findByIdInAndIsDeletedFalse(request.orderIds());
        if (orders.size() != request.orderIds().size()) {
            throw OrderNotFoundException.EXCEPTION;
        }
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
