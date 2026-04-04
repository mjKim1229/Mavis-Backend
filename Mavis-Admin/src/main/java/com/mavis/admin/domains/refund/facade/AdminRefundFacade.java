package com.mavis.admin.domains.refund.facade;

import com.mavis.admin.domains.refund.service.AdminRefundService;
import com.mavis.common.properties.TossPaymentsProperties;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.Payment;
import com.mavis.domain.domains.order.implement.PaymentReader;
import com.mavis.domain.domains.refund.domain.Refund;
import com.mavis.infrastructure.outer.api.tosspayments.client.PaymentsCancelClient;
import com.mavis.infrastructure.outer.api.tosspayments.dto.CancelPaymentsRequest;
import com.mavis.infrastructure.outer.api.tosspayments.dto.PaymentsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class AdminRefundFacade {

    private final TossPaymentsProperties tossPaymentsProperties;
    private final PaymentsCancelClient paymentsCancelClient;
    private final AdminRefundService adminRefundService;
    private final PaymentReader paymentReader;

    public void approveRefund(String idempotencyKey, String testCode, Long refundId) {
        Refund refund = adminRefundService.approveRefund(refundId);

        Order order = refund.getOrderItem().getOrder();
        Payment payment = paymentReader.findByOrder(order);

        String authorizationHeader = tossPaymentsProperties.getAuthorizationHeader();

        PaymentsResponse response = paymentsCancelClient.cancelPayments(
                authorizationHeader,
                idempotencyKey,
                testCode,
                payment.getPaymentKey(),
                new CancelPaymentsRequest(refund.getRefundReason(), refund.getRefundAmount())
        );

        log.info("Toss 환불 취소 응답: {}", response);

        String cancelTransactionKey = response.cancels() != null && !response.cancels().isEmpty()
                ? response.cancels().get(0).transactionKey()
                : null;

        adminRefundService.completeRefund(refund, cancelTransactionKey);
    }
}
