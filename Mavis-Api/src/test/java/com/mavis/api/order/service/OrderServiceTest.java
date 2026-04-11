package com.mavis.api.order.service;

import com.mavis.api.auth.implement.UserReader;
import com.mavis.api.order.dto.CreateOrderRequest;
import com.mavis.api.order.dto.OrderAddressRequest;
import com.mavis.api.order.implement.OrderItemAppender;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderStatus;
import com.mavis.domain.domains.order.exception.InvalidOrderInfoException;
import com.mavis.domain.domains.order.exception.PriceMismatchException;
import com.mavis.domain.domains.order.implement.OrderReader;
import com.mavis.domain.domains.order.implement.PaymentIdempotencyManager;
import com.mavis.domain.domains.order.repository.OrderRepository;
import com.mavis.domain.domains.order.repository.PaymentRepository;
import com.mavis.domain.domains.refund.implement.RefundAppender;
import com.mavis.domain.domains.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private UserReader userReader;
    @Mock
    private OrderItemAppender orderItemAppender;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private OrderReader orderReader;
    @Mock
    private RefundAppender refundAppender;
    @Mock
    private PaymentIdempotencyManager paymentIdempotencyManager;

    @Test
    void 주문자와_현재_유저가_다르면_InvalidOrderInfoException을_던진다() {
        // given
        String orderId = "ORDER-001";
        int requestAmount = 10000;

        User orderOwner = User.builder().id(1L).build();
        User currentUser = User.builder().id(2L).build();

        Order order = Order.builder()
                .orderId(orderId)
                .user(orderOwner)
                .orderStatus(OrderStatus.READY)
                .build();
        order.setTotalPrice(requestAmount);

        given(orderRepository.findByOrderIdAndIsDeletedFalse(orderId)).willReturn(Optional.of(order));
        given(userReader.getCurrentUser()).willReturn(currentUser);

        // when & then
        assertThatThrownBy(() -> orderService.validateAndMarkPaymentRequested(orderId, requestAmount))
                .isInstanceOf(InvalidOrderInfoException.class);
    }

    @Test
    void 요청_금액이_주문_금액과_다르면_PriceMismatchException을_던진다() {
        // given
        String orderId = "ORDER-001";
        int orderTotalPrice = 10000;
        int requestAmount = 99999;

        User orderOwner = User.builder().id(1L).build();

        Order order = Order.builder()
                .orderId(orderId)
                .user(orderOwner)
                .orderStatus(OrderStatus.READY)
                .build();
        order.setTotalPrice(orderTotalPrice);

        given(orderRepository.findByOrderIdAndIsDeletedFalse(orderId)).willReturn(Optional.of(order));
        given(userReader.getCurrentUser()).willReturn(orderOwner);

        // when & then
        assertThatThrownBy(() -> orderService.validateAndMarkPaymentRequested(orderId, requestAmount))
                .isInstanceOf(PriceMismatchException.class);
    }

    @Test
    void 아이템_총액과_배송비_합산이_요청_금액과_다르면_PriceMismatchException을_던진다() {
        // given
        int itemsTotalPrice = 10000;
        int deliveryFee = 4000;
        int requestAmount = 99999;

        OrderAddressRequest addressRequest = new OrderAddressRequest("홍길동", "010-1234-5678", "12345", "서울시", "101호", "문 앞");
        CreateOrderRequest request = new CreateOrderRequest(requestAmount, addressRequest, List.of());

        User user = User.builder().id(1L).build();
        given(userReader.getCurrentUser()).willReturn(user);
        given(orderRepository.saveAndFlush(any())).willAnswer(invocation -> invocation.getArgument(0));
        given(orderItemAppender.saveOrderItems(any(), any())).willReturn(itemsTotalPrice);

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(PriceMismatchException.class);
    }
}
