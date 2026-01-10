package com.mavis.api.order.service;

import com.mavis.api.auth.implement.UserReader;
import com.mavis.api.common.page.PageResponse;
import com.mavis.api.order.dto.CreateOrderRequest;
import com.mavis.api.order.dto.PendingOrderRequest;
import com.mavis.api.order.dto.OrderAddressRequest;
import com.mavis.api.order.dto.UserOrderInfo;
import com.mavis.api.order.implement.OrderItemAppender;
import com.mavis.common.properties.TossPaymentsProperties;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderAddress;
import com.mavis.domain.domains.order.domain.PendingOrder;
import com.mavis.domain.domains.order.exception.InvalidOrderInfoException;
import com.mavis.domain.domains.order.repository.OrderRepository;
import com.mavis.domain.domains.order.repository.PendingOrderRepository;
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
    private final PendingOrderRepository pendingOrderRepository;
    private final PaymentsConfirmClient paymentsConfirmClient;
    private final PaymentsCancelClient paymentsCancelClient;
    private final TossPaymentsProperties tossPaymentsProperties;

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

    public void confirmPayments(ConfirmPaymentRequest request) {
        String authorizationHeader = "Basic " + Base64.getEncoder()
                .encodeToString((tossPaymentsProperties.secretKey() + ":").getBytes(StandardCharsets.UTF_8));
        try {
            PaymentsResponse paymentsResponse = paymentsConfirmClient.confirmPayments(authorizationHeader, request);
            //TODO TossPayments Table 저장
        } catch (Exception e) {
            paymentsCancelClient.cancelPayments(authorizationHeader, request.paymentKey(), new CancelPaymentsRequest("결제 취소"));
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<UserOrderInfo> getUserOrderList(Pageable pageable) {
        User user = userReader.getCurrentUser();
        Page<Order> orderPages = orderRepository.findOrderPagesByUser(pageable, user);
        Page<UserOrderInfo> userOrderInfoPages = orderPages.map(order -> {
                    OrderAddress orderAddress = order.getOrderAddress();
                    return UserOrderInfo.builder()
                            .address(orderAddress.getAddress())
                            .addressInfo(orderAddress.getAddressMemo())
                            .totalPrice(order.getTotalPrice())
                            .userName(user.getName())
                            .orderStatus(order.getOrderStatus())
                            .build();
                }
        );
        return PageResponse.of(userOrderInfoPages);
    }

    @Transactional
    public void createPendingOrder(PendingOrderRequest request) {
        PendingOrder pendingOrder = request.toEntity();
        pendingOrderRepository.save(pendingOrder);
    }

    @Transactional
    public void validatePendingOrder(PendingOrderRequest request) {
        PendingOrder pendingOrder = pendingOrderRepository.findByOrderIdAndAmountAndIsConfirmedFalse(request.orderId(), request.amount())
                .orElseThrow(() -> InvalidOrderInfoException.EXCEPTION);
        pendingOrder.confirmed();
    }
}
