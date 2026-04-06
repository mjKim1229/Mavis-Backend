package com.mavis.api.scheduler;

import com.mavis.common.properties.TossPaymentsProperties;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.Payment;
import com.mavis.infrastructure.outer.api.tosspayments.client.PaymentsQueryByOrderClient;
import com.mavis.infrastructure.outer.api.tosspayments.client.PaymentsQueryClient;
import com.mavis.infrastructure.outer.api.tosspayments.dto.PaymentsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentReconciliationScheduler {

    private final PaymentReconciliationService paymentReconciliationService;
    private final PaymentsQueryClient paymentsQueryClient;
    private final PaymentsQueryByOrderClient paymentsQueryByOrderClient;
    private final TossPaymentsProperties tossPaymentsProperties;

    @Scheduled(fixedDelay = 5 * 60 * 1000)
    public void reconcile() {
        String authorization = tossPaymentsProperties.getAuthorizationHeader();
        reconcileWaitingDeposit(authorization);
        reconcileReadyOrders(authorization);
    }

    private void reconcileWaitingDeposit(String authorization) {
        List<Payment> targets = paymentReconciliationService.findWaitingDepositTargets();
        log.info("[결제 보정][WAITING_FOR_DEPOSIT] 대상: {}건", targets.size());

        int successCount = 0;
        int failCount = 0;

        for (Payment payment : targets) {
            String paymentKey = payment.getPaymentKey();
            try {
                PaymentsResponse response = paymentsQueryClient.getPayment(authorization, paymentKey);
                paymentReconciliationService.reconcileWaitingDeposit(paymentKey, response.status());
                successCount++;
            } catch (Exception e) {
                log.warn("[결제 보정][WAITING_FOR_DEPOSIT] 실패 - paymentKey: {}, 사유: {}", paymentKey, e.getMessage());
                failCount++;
            }
        }

        log.info("[결제 보정][WAITING_FOR_DEPOSIT] 완료 - 성공: {}건, 실패: {}건", successCount, failCount);
    }

    private void reconcileReadyOrders(String authorization) {
        List<Order> targets = paymentReconciliationService.findPaymentRequestedTargets();
        log.info("[결제 보정][PAYMENT_REQUESTED] 대상: {}건", targets.size());

        int successCount = 0;
        int failCount = 0;

        for (Order order : targets) {
            String orderId = order.getOrderId();
            try {
                PaymentsResponse response = paymentsQueryByOrderClient.getPaymentByOrderId(authorization, orderId);
                paymentReconciliationService.reconcileReadyOrder(order, response);
                successCount++;
            } catch (Exception e) {
                log.warn("[결제 보정][PAYMENT_REQUESTED] 실패 - orderId: {}, 사유: {}", orderId, e.getMessage());
                failCount++;
            }
        }

        log.info("[결제 보정][PAYMENT_REQUESTED] 완료 - 성공: {}건, 실패: {}건", successCount, failCount);
    }
}
