package com.mavis.domain.domains.order.domain;

import com.mavis.domain.domains.order.exception.InvalidOrderStatusTransitionException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderStatusTransitionTest {

    private Order orderWith(OrderStatus status) {
        return Order.builder()
                .orderId("GARAM-TEST")
                .orderStatus(status)
                .build();
    }

    @ParameterizedTest
    @CsvSource({
            "READY, PAYMENT_REQUESTED",
            "PAYMENT_REQUESTED, WAITING_FOR_DEPOSIT",
            "PAYMENT_REQUESTED, PAYMENT_CONFIRMED",
            "PAYMENT_REQUESTED, CANCELED",
            "WAITING_FOR_DEPOSIT, PAYMENT_CONFIRMED",
            "WAITING_FOR_DEPOSIT, CANCELED",
            "PAYMENT_CONFIRMED, ORDERED",
            "PAYMENT_CONFIRMED, CANCELED"
    })
    void 허용된_전이(OrderStatus from, OrderStatus to) {
        assertThat(from.canTransitionTo(to)).isTrue();
    }

    @ParameterizedTest
    @CsvSource({
            "READY, PAYMENT_CONFIRMED",
            "READY, CANCELED",
            "READY, WAITING_FOR_DEPOSIT",
            "READY, ORDERED",
            "PAYMENT_REQUESTED, ORDERED",
            "WAITING_FOR_DEPOSIT, ORDERED",
            "WAITING_FOR_DEPOSIT, PAYMENT_REQUESTED",
            "PAYMENT_CONFIRMED, PAYMENT_REQUESTED",
            "PAYMENT_CONFIRMED, WAITING_FOR_DEPOSIT",
            "ORDERED, CANCELED",
            "ORDERED, PAYMENT_CONFIRMED",
            "CANCELED, PAYMENT_CONFIRMED",
            "CANCELED, ORDERED",
            "CANCELED, PAYMENT_REQUESTED"
    })
    void 금지된_전이(OrderStatus from, OrderStatus to) {
        assertThat(from.canTransitionTo(to)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(OrderStatus.class)
    void 같은_상태로의_전이는_허용되지_않는다(OrderStatus status) {
        assertThat(status.canTransitionTo(status)).isFalse();
    }

    @Test
    void 종료상태에서는_어떤_전이도_불가능하다() {
        for (OrderStatus next : OrderStatus.values()) {
            assertThat(OrderStatus.ORDERED.canTransitionTo(next)).isFalse();
            assertThat(OrderStatus.CANCELED.canTransitionTo(next)).isFalse();
        }
    }

    @Test
    void 취소된_주문은_결제완료로_되돌릴_수_없다() {
        Order order = orderWith(OrderStatus.CANCELED);

        assertThatThrownBy(order::confirmPayment)
                .isInstanceOf(InvalidOrderStatusTransitionException.class);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELED);
    }

    @Test
    void 발주된_주문은_취소할_수_없다() {
        Order order = orderWith(OrderStatus.ORDERED);

        assertThatThrownBy(order::cancel)
                .isInstanceOf(InvalidOrderStatusTransitionException.class);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.ORDERED);
    }

    @Test
    void 미결제_주문은_발주할_수_없다() {
        Order order = orderWith(OrderStatus.READY);

        assertThatThrownBy(order::confirmOrder)
                .isInstanceOf(InvalidOrderStatusTransitionException.class);
        assertThat(order.getOrderedAt()).isNull();
    }

    @Test
    void 정상_결제_흐름은_끝까지_진행된다() {
        Order order = orderWith(OrderStatus.READY);

        order.paymentRequested();
        order.confirmPayment();
        order.confirmOrder();

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.ORDERED);
        assertThat(order.getOrderedAt()).isNotNull();
    }

    @Test
    void 가상계좌_흐름은_입금대기를_거쳐_결제완료된다() {
        Order order = orderWith(OrderStatus.READY);

        order.paymentRequested();
        order.waitingForDeposit();
        order.confirmPayment();

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PAYMENT_CONFIRMED);
    }
}
