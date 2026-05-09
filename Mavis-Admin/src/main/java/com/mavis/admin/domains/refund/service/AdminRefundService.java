package com.mavis.admin.domains.refund.service;

import com.mavis.admin.common.page.PageResponse;
import com.mavis.admin.domains.refund.dto.GetAdminRefundResponse;
import com.mavis.admin.domains.refund.dto.RefundValidateInfo;
import com.mavis.domain.domains.order.domain.CardInfo;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.Payment;
import com.mavis.domain.domains.order.domain.PaymentMethod;
import com.mavis.domain.domains.order.domain.PaymentType;
import com.mavis.domain.domains.order.domain.RefundReceiveAccount;
import com.mavis.infrastructure.outer.api.tosspayments.dto.PaymentsCancels;
import com.mavis.infrastructure.outer.api.tosspayments.dto.PaymentsResponse;
import com.mavis.domain.domains.order.exception.PaymentNotFoundException;
import com.mavis.domain.domains.order.repository.PaymentRepository;
import com.mavis.domain.domains.refund.domain.Refund;
import com.mavis.domain.domains.refund.domain.RefundStatus;
import com.mavis.domain.domains.refund.exception.CannotRefundException;
import com.mavis.domain.domains.refund.implement.RefundReader;
import com.mavis.domain.domains.refund.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminRefundService {

    private final RefundRepository refundRepository;
    private final RefundReader refundReader;
    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public PageResponse<GetAdminRefundResponse> getRefundList(Pageable pageable, RefundStatus refundStatus) {
        Page<Refund> refundPages = refundStatus == null
                ? refundRepository.findAll(pageable)
                : refundRepository.findByRefundStatus(refundStatus, pageable);
        return PageResponse.of(refundPages.map(GetAdminRefundResponse::from));
    }

    @Transactional(readOnly = true)
    public RefundValidateInfo validateForApproval(Long refundId) {
        Refund refund = refundReader.findByIdWithOrderItemAndOrder(refundId);
        if (refund.getRefundStatus() != RefundStatus.REQUESTED) {
            throw CannotRefundException.EXCEPTION;
        }
        Order order = refund.getOrderItem().getOrder();
        Payment confirmPayment = paymentRepository.findByOrderAndPaymentType(order, PaymentType.CONFIRM)
                .orElseThrow(() -> PaymentNotFoundException.EXCEPTION);
        RefundReceiveAccount refundReceiveAccount = confirmPayment.getRefundReceiveAccount();
        return new RefundValidateInfo(
                refund.getId(),
                order.getId(),
                refund.getRefundAmount(),
                refund.getRefundReason(),
                confirmPayment.getPaymentKey(),
                refundReceiveAccount != null ? refundReceiveAccount.getRefundReceiveBankCode() : null,
                refundReceiveAccount != null ? refundReceiveAccount.getRefundReceiveAccountNumber() : null,
                refundReceiveAccount != null ? refundReceiveAccount.getRefundReceiveHolderName() : null
        );
    }

    @Transactional
    public void approveAndComplete(Long refundId, PaymentsResponse response, PaymentsCancels cancelEntry) {
        Refund refund = refundReader.findByIdWithOrderItemAndOrder(refundId);
        Order order = refund.getOrderItem().getOrder();

        Payment cancelPayment = Payment.builder()
                .order(order)
                .paymentType(PaymentType.CANCEL)
                .paymentKey(response.paymentKey())
                .tossOrderId(response.orderId())
                .orderName(response.orderName())
                .provider(response.easyPayProvider())
                .method(PaymentMethod.from(response.method()))
                .totalAmount(cancelEntry.cancelAmount())
                .balanceAmount(response.balanceAmount())
                .requestedAt(response.requestedAt().toLocalDateTime())
                .canceledAt(cancelEntry.canceledAt().toLocalDateTime())
                .lastTransactionKey(cancelEntry.transactionKey())
                .partialCancelable(response.isPartialCancelable())
                .cardInfo(new CardInfo(response.cardNumber(), response.cardIssuerCode()))
                .receiptUrl(response.receiptUrl())
                .cancelAmount(cancelEntry.cancelAmount())
                .cancelReason(cancelEntry.cancelReason())
                .build();

        Payment saved = paymentRepository.save(cancelPayment);
        refund.approve();
        refund.linkPayment(saved);
        refund.complete(cancelEntry.transactionKey());

        boolean allRefunded = order.getOrderItems().stream()
                .allMatch(item -> item.getRefund() != null
                        && item.getRefund().getRefundStatus() == RefundStatus.COMPLETED);
        if (allRefunded) {
            order.cancel();
        }
    }

    @Transactional
    public void rejectRefund(Long refundId) {
        Refund refund = refundReader.findById(refundId);
        if (refund.getRefundStatus() != RefundStatus.REQUESTED) {
            throw CannotRefundException.EXCEPTION;
        }
        refund.reject();
    }
}
