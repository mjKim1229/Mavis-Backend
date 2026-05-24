package com.mavis.admin.domains.order.service;

import com.mavis.admin.domains.order.dto.AdminOrderConfirmRequest;
import com.mavis.admin.domains.order.dto.AdminOrderCountResponse;
import com.mavis.admin.support.ControllerTestSupport;
import com.mavis.domain.domains.delivery.domain.Delivery;
import com.mavis.domain.domains.delivery.domain.DeliveryStatus;
import com.mavis.domain.domains.delivery.repository.DeliveryRepository;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderAddress;
import com.mavis.domain.domains.order.domain.OrderStatus;
import com.mavis.domain.domains.order.repository.OrderRepository;
import com.mavis.domain.domains.refund.domain.Refund;
import com.mavis.domain.domains.refund.domain.RefundStatus;
import com.mavis.domain.domains.refund.repository.RefundRepository;
import com.mavis.domain.domains.user.domain.User;
import com.mavis.domain.domains.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdminOrderServiceTest extends ControllerTestSupport {

    @Autowired private AdminOrderService adminOrderService;
    @Autowired private OrderRepository orderRepository;
    @Autowired private DeliveryRepository deliveryRepository;
    @Autowired private RefundRepository refundRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private EntityManager em;

    private User savedUser;

    @BeforeEach
    void setUp() {
        savedUser = userRepository.save(User.builder()
                .name("테스트유저")
                .email("test@test.com")
                .build());
    }

    private Order createOrder(String orderId, OrderStatus status) {
        return orderRepository.save(Order.builder()
                .orderId(orderId)
                .user(savedUser)
                .totalPrice(50000)
                .orderStatus(status)
                .orderAddress(new OrderAddress("수신자", "010-1234-5678", "12345", "서울시 강남구", "101호", ""))
                .build());
    }

    @Nested
    class 주문_카운트_조회 {

        @Test
        void 각_상태별_카운트_정확히_반환() {
            createOrder("GARAM0001", OrderStatus.PAYMENT_CONFIRMED);
            createOrder("GARAM0002", OrderStatus.PAYMENT_CONFIRMED);

            Order ordered = createOrder("GARAM0003", OrderStatus.ORDERED);
            deliveryRepository.save(Delivery.builder().order(ordered).deliveryStatus(DeliveryStatus.READY).build());

            Order shipped1 = createOrder("GARAM0004", OrderStatus.ORDERED);
            Order shipped2 = createOrder("GARAM0005", OrderStatus.ORDERED);
            deliveryRepository.save(Delivery.builder().order(shipped1).deliveryStatus(DeliveryStatus.SHIPPED).build());
            deliveryRepository.save(Delivery.builder().order(shipped2).deliveryStatus(DeliveryStatus.SHIPPED).build());

            Order delivered = createOrder("GARAM0006", OrderStatus.ORDERED);
            deliveryRepository.save(Delivery.builder().order(delivered).deliveryStatus(DeliveryStatus.DELIVERED).build());

            refundRepository.save(Refund.builder().refundStatus(RefundStatus.REQUESTED).build());
            refundRepository.save(Refund.builder().refundStatus(RefundStatus.REQUESTED).build());
            refundRepository.save(Refund.builder().refundStatus(RefundStatus.REQUESTED).build());
            em.flush();
            em.clear();

            AdminOrderCountResponse response = adminOrderService.getOrderCounts();

            assertThat(response.paymentConfirmedCount()).isEqualTo(2);
            assertThat(response.orderedCount()).isEqualTo(1);
            assertThat(response.shippedCount()).isEqualTo(2);
            assertThat(response.deliveredCount()).isEqualTo(1);
            assertThat(response.refundRequestedCount()).isEqualTo(3);
        }

        @Test
        void 데이터_없을때_모두_0_반환() {
            AdminOrderCountResponse response = adminOrderService.getOrderCounts();

            assertThat(response.paymentConfirmedCount()).isZero();
            assertThat(response.orderedCount()).isZero();
            assertThat(response.shippedCount()).isZero();
            assertThat(response.deliveredCount()).isZero();
            assertThat(response.refundRequestedCount()).isZero();
        }
    }

    @Nested
    class 주문_발주_확인 {

        @Test
        void 여러_주문_상태_ORDERED로_변경() {
            Order order1 = createOrder("GARAM0010", OrderStatus.PAYMENT_CONFIRMED);
            Order order2 = createOrder("GARAM0011", OrderStatus.PAYMENT_CONFIRMED);
            em.flush();
            em.clear();

            adminOrderService.confirmOrder(new AdminOrderConfirmRequest(List.of(order1.getId(), order2.getId())));
            em.flush();
            em.clear();

            Order updated1 = orderRepository.findById(order1.getId()).orElseThrow();
            Order updated2 = orderRepository.findById(order2.getId()).orElseThrow();
            assertThat(updated1.getOrderStatus()).isEqualTo(OrderStatus.ORDERED);
            assertThat(updated2.getOrderStatus()).isEqualTo(OrderStatus.ORDERED);
        }

        @Test
        void 발주_확인시_주문별_READY_배송_생성() {
            Order order1 = createOrder("GARAM0012", OrderStatus.PAYMENT_CONFIRMED);
            Order order2 = createOrder("GARAM0013", OrderStatus.PAYMENT_CONFIRMED);
            em.flush();
            em.clear();

            adminOrderService.confirmOrder(new AdminOrderConfirmRequest(List.of(order1.getId(), order2.getId())));
            em.flush();
            em.clear();

            Order reloaded1 = orderRepository.findById(order1.getId()).orElseThrow();
            Order reloaded2 = orderRepository.findById(order2.getId()).orElseThrow();
            Delivery delivery1 = deliveryRepository.findByOrder(reloaded1).orElseThrow();
            Delivery delivery2 = deliveryRepository.findByOrder(reloaded2).orElseThrow();
            assertThat(delivery1.getDeliveryStatus()).isEqualTo(DeliveryStatus.READY);
            assertThat(delivery2.getDeliveryStatus()).isEqualTo(DeliveryStatus.READY);
        }

        @Test
        void 존재하지_않는_주문ID_포함시_예외_발생() {
            Order order = createOrder("GARAM0014", OrderStatus.PAYMENT_CONFIRMED);
            em.flush();
            em.clear();

            assertThatThrownBy(() ->
                    adminOrderService.confirmOrder(new AdminOrderConfirmRequest(List.of(order.getId(), 999L)))
            ).isInstanceOf(com.mavis.domain.domains.order.exception.OrderNotFoundException.class);
        }
    }
}
