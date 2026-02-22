package com.mavis.api.order.service;

import com.mavis.api.auth.implement.UserReader;
import com.mavis.api.common.page.PageResponse;
import com.mavis.api.order.dto.CreateOrderRequest;
import com.mavis.api.order.dto.OrderAddressRequest;
import com.mavis.api.order.dto.UserOrderInfo;
import com.mavis.api.order.implement.OrderItemAppender;
import com.mavis.common.properties.TossPaymentsProperties;
import com.mavis.domain.domains.order.domain.*;
import com.mavis.domain.domains.order.exception.InvalidOrderInfoException;
import com.mavis.domain.domains.order.exception.OrderNotFoundException;
import com.mavis.domain.domains.order.exception.PriceMismatchException;
import com.mavis.domain.domains.order.implement.OrderReader;
import com.mavis.domain.domains.order.repository.OrderRepository;
import com.mavis.domain.domains.order.repository.PaymentRepository;
import com.mavis.domain.domains.user.domain.User;
import com.mavis.infrastructure.outer.api.tosspayments.client.PaymentsCancelClient;
import com.mavis.infrastructure.outer.api.tosspayments.client.PaymentsConfirmClient;
import com.mavis.infrastructure.outer.api.tosspayments.dto.CancelPaymentsRequest;
import com.mavis.infrastructure.outer.api.tosspayments.dto.ConfirmPaymentRequest;
import com.mavis.infrastructure.outer.api.tosspayments.dto.PaymentsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserReader userReader;
    private final OrderItemAppender orderItemAppender;
    private final PaymentsConfirmClient paymentsConfirmClient;
    private final PaymentsCancelClient paymentsCancelClient;
    private final TossPaymentsProperties tossPaymentsProperties;
    private final OrderReader orderReader;
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

    @Transactional
    public void confirmPayments(ConfirmPaymentRequest request) {
        String authorizationHeader = "Basic " + Base64.getEncoder()
                .encodeToString((tossPaymentsProperties.secretKey() + ":").getBytes(StandardCharsets.UTF_8));
        try {
            Order order = orderRepository.findByOrderIdAndIsDeletedFalse(request.orderId())
                    .orElseThrow(() -> OrderNotFoundException.EXCEPTION);

            if (order.getTotalPrice() != request.amount()) {
                throw PriceMismatchException.EXCEPTION;
            }

            if (order.isPayConfirmed()) {
                throw InvalidOrderInfoException.EXCEPTION;
            }

            PaymentsResponse response = paymentsConfirmClient.confirmPayments(authorizationHeader, request);
            if (!response.totalAmount().equals(request.amount())) {
                throw PriceMismatchException.EXCEPTION;
            }

            Payment payment = Payment.builder()
                    .order(order)
                    .paymentKey(response.paymentKey())
                    .method(PaymentMethod.valueOf(response.method().name()))
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
        } catch (Exception e) {
            paymentsCancelClient.cancelPayments(
                    authorizationHeader,
                    request.paymentKey(),
                    new CancelPaymentsRequest("결제 실패로 인한 자동 취소")
            );
            throw e;
        }
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
