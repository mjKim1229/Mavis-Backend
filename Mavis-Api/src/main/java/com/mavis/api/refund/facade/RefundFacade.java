package com.mavis.api.refund.facade;

import com.mavis.api.refund.dto.CreateRefundRequest;
import com.mavis.api.refund.service.RefundService;
import com.mavis.common.properties.TossPaymentsProperties;
import com.mavis.domain.domains.delivery.domain.Delivery;
import com.mavis.domain.domains.delivery.domain.DeliveryStatus;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.order.domain.OrderStatus;
import com.mavis.domain.domains.order.repository.PaymentRepository;
import com.mavis.domain.domains.refund.domain.Refund;
import com.mavis.domain.domains.refund.exception.CannotRefundException;
import com.mavis.infrastructure.outer.api.tosspayments.client.PaymentsCancelClient;
import com.mavis.infrastructure.outer.api.tosspayments.dto.CancelPaymentsRequest;
import com.mavis.infrastructure.outer.api.tosspayments.dto.PaymentsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefundFacade {

    private final TossPaymentsProperties tossPaymentsProperties;
    private final PaymentsCancelClient paymentsCancelClient;
    private final RefundService refundService;
    private final PaymentRepository paymentRepository;

    public void cancelRefund(Long orderItemId, CreateRefundRequest request) {
        Refund refund = refundService.createRefund(orderItemId, request);

        OrderItem orderItem = refund.getOrderItem();
        Order order = orderItem.getOrder();

        if (order.getOrderStatus() != OrderStatus.PAYMENT_CONFIRMED) {
            throw CannotRefundException.EXCEPTION;
        }

        cancelTossPayment(order, refund, request);
    }

    public void requestReturn(Long orderItemId, CreateRefundRequest request) {
        Refund refund = refundService.createRefund(orderItemId, request);

        OrderItem orderItem = refund.getOrderItem();
        Order order = orderItem.getOrder();

        Delivery delivery = order.getDelivery();
        if (delivery == null || delivery.getDeliveryStatus() != DeliveryStatus.DELIVERED) {
            throw CannotRefundException.EXCEPTION;
        }

        log.info("반품 요청 생성 - refundId: {}, orderItemId: {}", refund.getId(), orderItemId);
    }

    private void cancelTossPayment(Order order, Refund refund, CreateRefundRequest request) {
        String authorizationHeader = "Basic " + Base64.getEncoder()
                .encodeToString((tossPaymentsProperties.secretKey() + ":").getBytes(StandardCharsets.UTF_8));

        var payment = paymentRepository.findByOrder(order);
        PaymentsResponse response = paymentsCancelClient.cancelPayments(
                authorizationHeader,
                payment.getPaymentKey(),
                payment.getPaymentKey(),
                new CancelPaymentsRequest(request.refundReason(), refund.getRefundAmount())
        );

        log.info("Toss 부분 취소 응답: {}", response);

        String cancelTransactionKey = response.cancels() != null && !response.cancels().isEmpty()
                ? response.cancels().get(0).transactionKey()
                : null;
        refundService.completeRefund(refund, cancelTransactionKey);
    }
}
