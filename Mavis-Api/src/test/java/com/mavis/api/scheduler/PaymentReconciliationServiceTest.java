package com.mavis.api.scheduler;

import com.mavis.domain.domains.order.domain.*;
import com.mavis.domain.domains.order.repository.OrderRepository;
import com.mavis.domain.domains.order.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PaymentReconciliationServiceTest {

    @InjectMocks
    private PaymentReconciliationService paymentReconciliationService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Test
    void 만료된_가상계좌_주문이_있으면_CANCELED로_변경한다() {
        Order order = Order.builder()
                .orderId("ORDER-001")
                .build();
        order.waitingForDeposit();

        Payment payment = Payment.builder()
                .paymentKey("paymentKey-001")
                .paymentType(PaymentType.CONFIRM)
                .order(order)
                .virtualAccountInfo(new VirtualAccountInfo(
                        "accountNumber",
                        "bankCode",
                        LocalDateTime.now().minusMinutes(1),
                        null
                ))
                .build();

        given(paymentRepository.findExpiredVirtualAccountPayments(any(LocalDateTime.class)))
                .willReturn(List.of(payment));

        paymentReconciliationService.expireAllExpiredVirtualAccounts();

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELED);
    }

    @Test
    void 만료된_가상계좌_주문이_없으면_상태_변경_없다() {
        given(paymentRepository.findExpiredVirtualAccountPayments(any(LocalDateTime.class)))
                .willReturn(List.of());

        paymentReconciliationService.expireAllExpiredVirtualAccounts();
    }

    @Test
    void 만료된_가상계좌_주문이_여러개면_모두_CANCELED로_변경한다() {
        Order order1 = Order.builder().orderId("ORDER-001").build();
        Order order2 = Order.builder().orderId("ORDER-002").build();
        order1.waitingForDeposit();
        order2.waitingForDeposit();

        LocalDateTime expiredDueDate = LocalDateTime.now().minusHours(1);

        Payment payment1 = Payment.builder()
                .paymentKey("paymentKey-001")
                .paymentType(PaymentType.CONFIRM)
                .order(order1)
                .virtualAccountInfo(new VirtualAccountInfo("acc1", "bank1", expiredDueDate, null))
                .build();

        Payment payment2 = Payment.builder()
                .paymentKey("paymentKey-002")
                .paymentType(PaymentType.CONFIRM)
                .order(order2)
                .virtualAccountInfo(new VirtualAccountInfo("acc2", "bank2", expiredDueDate, null))
                .build();

        given(paymentRepository.findExpiredVirtualAccountPayments(any(LocalDateTime.class)))
                .willReturn(List.of(payment1, payment2));

        paymentReconciliationService.expireAllExpiredVirtualAccounts();

        assertThat(order1.getOrderStatus()).isEqualTo(OrderStatus.CANCELED);
        assertThat(order2.getOrderStatus()).isEqualTo(OrderStatus.CANCELED);
    }
}
