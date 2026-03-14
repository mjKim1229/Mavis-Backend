package com.mavis.api.order.service;

import com.mavis.api.auth.implement.UserReader;
import com.mavis.api.common.page.PageResponse;
import com.mavis.api.order.dto.CreateOrderRequest;
import com.mavis.api.order.dto.CreateOrderResponse;
import com.mavis.api.order.dto.OrderAddressRequest;
import com.mavis.api.order.dto.UserOrderInfo;
import com.mavis.api.order.implement.OrderItemAppender;
import com.mavis.common.util.OrderNumberGenerator;
import com.mavis.domain.domains.order.domain.*;
import com.mavis.domain.domains.order.exception.CannotCancelOrderException;
import com.mavis.domain.domains.order.exception.InvalidOrderInfoException;
import com.mavis.domain.domains.order.exception.OrderNotFoundException;
import com.mavis.domain.domains.order.exception.PriceMismatchException;
import com.mavis.domain.domains.order.implement.OrderReader;
import com.mavis.domain.domains.order.repository.OrderRepository;
import com.mavis.domain.domains.order.repository.PaymentRepository;
import com.mavis.domain.domains.user.domain.User;
import com.mavis.infrastructure.outer.api.tosspayments.dto.PaymentsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.mavis.common.util.OrderNumberGenerator.DOMAIN_PREFIX;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserReader userReader;
    private final OrderItemAppender orderItemAppender;
    private final PaymentRepository paymentRepository;
    private static final int DELIVERY_FEE = 4000;
    private final OrderReader orderReader;

    @Transactional
    @Retryable(
            retryFor = { DataIntegrityViolationException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        User user = userReader.getCurrentUser();
        OrderAddressRequest orderAddressRequest = request.orderAddressRequest();
        Order order = Order.builder()
                .orderId(OrderNumberGenerator.generateOrderId())
                .user(user)
                .orderAddress(orderAddressRequest.toOrderAddress())
                .build();
        Order savedOrder = orderRepository.saveAndFlush(order);

        int itemsTotalPrice = orderItemAppender.saveOrderItems(request.orderItems(), savedOrder);
        int totalPrice = itemsTotalPrice + DELIVERY_FEE;
        if (totalPrice != request.amount()) {
            throw PriceMismatchException.EXCEPTION;
        }
        order.setTotalPrice(totalPrice);
        return CreateOrderResponse.from(order.getOrderId());
    }

    @Transactional(readOnly = true)
    public Order validateOrderForPayment(String orderId, Long amount) {
        Order order = orderRepository.findByOrderIdAndIsDeletedFalse(orderId)
                .orElseThrow(() -> OrderNotFoundException.EXCEPTION);

        User user = userReader.getCurrentUser();
        if (!order.getUser().equals(user)) {
            throw InvalidOrderInfoException.EXCEPTION;
        }

        if (order.getTotalPrice() != amount) {
            throw PriceMismatchException.EXCEPTION;
        }

        if (order.getOrderStatus() != OrderStatus.READY) {
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
        order.confirmPayment();
    }

    @Transactional
    public void cancelOrder(Order order) {
        order.cancel();
    }

    @Transactional(readOnly = true)
    public Order findOrderToCancel(Long orderId) {
        Order order = orderReader.findOrderById(orderId);
        if (!order.getOrderStatus().equals(OrderStatus.PAYMENT_CONFIRMED)) {
            throw CannotCancelOrderException.EXCEPTION;
        }
        return order;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserOrderInfo> getUserOrderList(Pageable pageable) {
        User user = userReader.getCurrentUser();
        Page<Order> orderPages = orderRepository.findOrderPagesByUser(pageable, user);
        Page<UserOrderInfo> userOrderInfoPages = orderPages.map(o -> {
                    OrderAddress orderAddress = o.getOrderAddress();
                    return UserOrderInfo.builder()
                            .orderId(o.getOrderId().substring(DOMAIN_PREFIX.length()))
                            .address(orderAddress.getAddress())
                            .addressInfo(orderAddress.getAddressDetail())
                            .totalPrice(o.getTotalPrice())
                            .userName(o.getUser().getName())
                            .orderStatus(o.getOrderStatus().getTitle())
                            .build();
                }
        );
        return PageResponse.of(userOrderInfoPages);
    }
}
