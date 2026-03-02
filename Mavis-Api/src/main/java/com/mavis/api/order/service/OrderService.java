package com.mavis.api.order.service;

import com.mavis.api.auth.implement.UserReader;
import com.mavis.api.common.page.PageResponse;
import com.mavis.api.order.dto.CreateOrderRequest;
import com.mavis.api.order.dto.OrderAddressRequest;
import com.mavis.api.order.dto.UserOrderInfo;
import com.mavis.api.order.implement.OrderItemAppender;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderAddress;
import com.mavis.domain.domains.order.domain.Payment;
import com.mavis.domain.domains.order.domain.PaymentMethod;
import com.mavis.domain.domains.order.exception.InvalidOrderInfoException;
import com.mavis.domain.domains.order.exception.OrderNotFoundException;
import com.mavis.domain.domains.order.exception.PriceMismatchException;
import com.mavis.domain.domains.order.repository.OrderRepository;
import com.mavis.domain.domains.order.repository.PaymentRepository;
import com.mavis.domain.domains.user.domain.User;
import com.mavis.infrastructure.outer.api.tosspayments.dto.PaymentsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserReader userReader;
    private final OrderItemAppender orderItemAppender;
    private final PaymentRepository paymentRepository;
    private static final int DELIVERY_FEE = 4000;

    @Transactional
    public void createOrder(CreateOrderRequest request) {
        User user = userReader.getCurrentUser();
        OrderAddressRequest orderAddressRequest = request.orderAddressRequest();
        Order order = Order.builder()
                .orderId(request.orderId())
                .user(user)
                .orderAddress(orderAddressRequest.toOrderAddress())
                .build();
        Order savedOrder = orderRepository.save(order);
        int itemsTotalPrice = orderItemAppender.saveOrderItems(request.orderItems(), savedOrder);
        int totalPrice = itemsTotalPrice + DELIVERY_FEE;
        if (totalPrice != request.amount()) {
            throw PriceMismatchException.EXCEPTION;
        }
        order.setTotalPrice(totalPrice);
    }

    @Transactional(readOnly = true)
    public Order validateOrderForPayment(String orderId, Long amount) {
        Order order = orderRepository.findByOrderIdAndIsDeletedFalse(orderId)
                .orElseThrow(() -> OrderNotFoundException.EXCEPTION);
        if (order.getTotalPrice() != amount) {
            throw PriceMismatchException.EXCEPTION;
        }
        if (order.isPayConfirmed()) {
            throw InvalidOrderInfoException.EXCEPTION;
        }
        return order;
    }

    @Transactional
    public void processPaymentSuccess(Order order, PaymentsResponse response) {
        if (response.totalAmount() != order.getTotalPrice()) {
            throw PriceMismatchException.EXCEPTION;
        }

        Payment payment = Payment.builder()
                .order(order)
                .paymentKey(response.paymentKey())
                .method(PaymentMethod.from(response.method()))
                .totalAmount(response.totalAmount())
                .balanceAmount(response.balanceAmount())
                .requestedAt(response.requestedAt())
                .approvedAt(response.approvedAt())
                .lastTransactionKey(response.lastTransactionKey())
                .partialCancelable(response.isPartialCancelable())
                .cardNumber(response.card() != null ? response.card().number() : null)
                .receiptUrl(response.receipt() != null ? response.receipt().url() : null)
                .build();

        paymentRepository.save(payment);
        order.setPayConfirmed();
    }

    @Transactional(readOnly = true)
    public PageResponse<UserOrderInfo> getUserOrderList(Pageable pageable) {
        User user = userReader.getCurrentUser();
        Page<Order> orderPages = orderRepository.findOrderPagesByUser(pageable, user);
        Page<UserOrderInfo> userOrderInfoPages = orderPages.map(o -> {
                    OrderAddress orderAddress = o.getOrderAddress();
                    return UserOrderInfo.builder()
                            .address(orderAddress.getAddress())
                            .addressInfo(orderAddress.getAddressDetail())
                            .totalPrice(o.getTotalPrice())
                            .userName(o.getUser().getName())
                            .orderStatus(o.getOrderStatus())
                            .build();
                }
        );
        return PageResponse.of(userOrderInfoPages);
    }
}
