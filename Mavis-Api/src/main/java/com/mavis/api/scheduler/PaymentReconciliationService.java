package com.mavis.api.scheduler;

import com.mavis.domain.domains.order.domain.*;
import com.mavis.domain.domains.order.exception.PaymentNotFoundException;
import com.mavis.domain.domains.order.repository.OrderRepository;
import com.mavis.domain.domains.order.repository.PaymentRepository;
import com.mavis.infrastructure.outer.api.tosspayments.dto.PaymentsResponse;
import com.mavis.infrastructure.outer.api.tosspayments.dto.PaymentsStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentReconciliationService {

    private static final int EXPIRY_MINUTES = 15;

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public List<Payment> findWaitingDepositTargets() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(EXPIRY_MINUTES);
        return paymentRepository.findByOrderOrderStatusAndOrderCreatedAtBefore(
                OrderStatus.WAITING_FOR_DEPOSIT, threshold);
    }

    @Transactional(readOnly = true)
    public List<Order> findPaymentRequestedTargets() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(EXPIRY_MINUTES);
        return orderRepository.findByOrderStatusAndCreatedAtBefore(OrderStatus.PAYMENT_REQUESTED, threshold);
    }

    @Transactional
    public void reconcileWaitingDeposit(String paymentKey, PaymentsStatus tossStatus) {
        Payment payment = paymentRepository.findByPaymentKey(paymentKey)
                .orElseThrow(() -> PaymentNotFoundException.EXCEPTION);

        Order order = payment.getOrder();

        switch (tossStatus) {
            case DONE -> {
                order.confirmPayment();
                log.info("[결제 보정] 입금 완료 처리 - orderId: {}", order.getOrderId());
            }
            case EXPIRED, CANCELED, ABORTED -> {
                order.cancel();
                log.info("[결제 보정] 주문 취소 처리 - orderId: {}, tossStatus: {}", order.getOrderId(), tossStatus);
            }
            default -> log.debug("[결제 보정] 변경 없음 - orderId: {}, tossStatus: {}", order.getOrderId(), tossStatus);
        }
    }

    @Transactional
    public void reconcileReadyOrder(Order order, PaymentsResponse response) {
        switch (response.status()) {
            case DONE, WAITING_FOR_DEPOSIT -> {
                Payment payment = Payment.builder()
                        .order(order)
                        .paymentType(PaymentType.CONFIRM)
                        .paymentKey(response.paymentKey())
                        .tossOrderId(response.orderId())
                        .orderName(response.orderName())
                        .provider(response.easyPayProvider())
                        .method(PaymentMethod.from(response.method()))
                        .totalAmount(response.totalAmount())
                        .balanceAmount(response.balanceAmount())
                        .requestedAt(response.requestedAt().toLocalDateTime())
                        .approvedAt(response.approvedAtLocal())
                        .lastTransactionKey(response.lastTransactionKey())
                        .partialCancelable(response.isPartialCancelable())
                        .cardInfo(new CardInfo(response.cardNumber(), response.cardIssuerCode()))
                        .receiptUrl(response.receiptUrl())
                        .virtualAccountSecret(response.secret())
                        .build();
                paymentRepository.save(payment);

                if (response.status() == PaymentsStatus.WAITING_FOR_DEPOSIT) {
                    order.waitingForDeposit();
                } else {
                    order.confirmPayment();
                }
                log.info("[결제 보정] READY 주문 복구 - orderId: {}, tossStatus: {}", order.getOrderId(), response.status());
            }
            case CANCELED, ABORTED, EXPIRED -> {
                order.cancel();
                log.info("[결제 보정] READY 주문 취소 처리 - orderId: {}, tossStatus: {}", order.getOrderId(), response.status());
            }
            default -> log.debug("[결제 보정] READY 변경 없음 - orderId: {}, tossStatus: {}", order.getOrderId(), response.status());
        }
    }
}
