package com.mavis.api.order.service;

import com.mavis.api.auth.implement.UserReader;
import com.mavis.api.order.dto.CreateOrderRequest;
import com.mavis.api.order.dto.OrderAddressRequest;
import com.mavis.api.order.implement.OrderItemAppender;
import com.mavis.domain.domains.order.domain.*;
import com.mavis.domain.domains.order.exception.InvalidOrderInfoException;
import com.mavis.domain.domains.order.exception.OrderAmountExceededException;
import com.mavis.domain.domains.order.exception.PriceMismatchException;
import com.mavis.domain.domains.order.implement.OrderReader;
import com.mavis.domain.domains.order.implement.PaymentIdempotencyManager;
import com.mavis.domain.domains.order.repository.OrderRepository;
import com.mavis.domain.domains.order.repository.PaymentRepository;
import com.mavis.domain.domains.claim.repository.ClaimRepository;
import com.mavis.domain.domains.refund.repository.RefundRepository;
import com.mavis.domain.domains.user.domain.User;
import com.mavis.infrastructure.outer.api.tosspayments.dto.VirtualAccountDepositCallbackRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

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
    private ClaimRepository claimRepository;
    @Mock
    private RefundRepository refundRepository;
    @Mock
    private PaymentIdempotencyManager paymentIdempotencyManager;

    @Test
    void 주문자와_현재_유저가_다르면_InvalidOrderInfoException을_던진다() {
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

        assertThatThrownBy(() -> orderService.validateAndMarkPaymentRequested(orderId, requestAmount))
                .isInstanceOf(InvalidOrderInfoException.class);
    }

    @Test
    void 요청_금액이_주문_금액과_다르면_PriceMismatchException을_던진다() {
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

    @Test
    void 아이템_총액과_배송비_합산이_int_범위를_넘으면_OrderAmountExceededException을_던진다() {
        // given: 아이템 총액이 Integer.MAX_VALUE — 배송비를 더하면 음수로 뒤집히는 경계
        int itemsTotalPrice = Integer.MAX_VALUE;

        OrderAddressRequest addressRequest = new OrderAddressRequest("홍길동", "010-1234-5678", "12345", "서울시", "101호", "문 앞");
        CreateOrderRequest request = new CreateOrderRequest(10000, addressRequest, List.of());

        User user = User.builder().id(1L).build();
        given(userReader.getCurrentUser()).willReturn(user);
        given(orderRepository.saveAndFlush(any())).willAnswer(invocation -> invocation.getArgument(0));
        given(orderItemAppender.saveOrderItems(any(), any())).willReturn(itemsTotalPrice);

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(OrderAmountExceededException.class);
    }

    @Test
    void 취소된_주문에_입금_콜백이_오면_상태를_되살리지_않고_무시한다() {
        // given: 가상계좌 입금 콜백은 토스 웹훅 — 예외를 던지면 재시도가 반복되므로 조용히 무시한다
        String tossOrderId = "ORDER-CANCELED";
        Order order = Order.builder()
                .orderId(tossOrderId)
                .orderStatus(OrderStatus.CANCELED)
                .build();
        VirtualAccountDepositCallbackRequest request = new VirtualAccountDepositCallbackRequest(
                LocalDateTime.now(), "secret-001", "DONE", "txn-001", tossOrderId);

        given(orderRepository.findByOrderIdAndIsDeletedFalse(tossOrderId)).willReturn(Optional.of(order));

        // when
        orderService.processDepositCallback(request);

        // then
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELED);
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void 입금대기가_아닌_주문의_입금_콜백은_무시한다() {
        // given: 승인 후처리 실패 등으로 PAYMENT_REQUESTED에 멈춘 주문 — 입금 콜백으로 결제완료시키지 않는다
        String tossOrderId = "ORDER-REQUESTED";
        Order order = Order.builder()
                .orderId(tossOrderId)
                .orderStatus(OrderStatus.PAYMENT_REQUESTED)
                .build();
        VirtualAccountDepositCallbackRequest request = new VirtualAccountDepositCallbackRequest(
                LocalDateTime.now(), "secret-001", "DONE", "txn-001", tossOrderId);

        given(orderRepository.findByOrderIdAndIsDeletedFalse(tossOrderId)).willReturn(Optional.of(order));

        // when
        orderService.processDepositCallback(request);

        // then
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PAYMENT_REQUESTED);
        verify(paymentRepository, never()).save(any());
    }
}
