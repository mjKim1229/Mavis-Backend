package com.mavis.domain.domains.order.repository;

import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.order.dto.AdminOrderItemRow;
import com.mavis.domain.domains.order.dto.AdminOrderRow;
import com.mavis.domain.domains.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderCustomRepository {

    Page<AdminOrderRow> findPaymentConfirmedOrderRows(Pageable pageable);

    Page<AdminOrderRow> findOrderedOrderRows(Pageable pageable);

    List<AdminOrderItemRow> findOrderItemRowsByOrderIds(List<Long> orderIds);

    Page<Order> findOrderPagesByUser(Pageable pageable, User user);

    Page<OrderItem> findUserOrderItemCanReview(Pageable pageable, User user);

    long countOrderedWithReadyDelivery();
}
