package com.mavis.admin.domains.refund.service;

import com.mavis.admin.common.page.PageResponse;
import com.mavis.admin.domains.refund.dto.GetAdminRefundResponse;
import com.mavis.admin.domains.refund.dto.RefundValidateInfo;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.Payment;
import com.mavis.domain.domains.order.domain.PaymentType;
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
        return new RefundValidateInfo(
                refund.getId(),
                order.getId(),
                refund.getRefundAmount(),
                refund.getRefundReason(),
                confirmPayment.getPaymentKey()
        );
    }

    @Transactional
    public void approveAndComplete(Long refundId, String cancelTransactionKey) {
        Refund refund = refundReader.findByIdWithOrderItemAndOrder(refundId);
        Order order = refund.getOrderItem().getOrder();

        Payment cancelPayment = Payment.builder()
                .order(order)
                .paymentType(PaymentType.CANCEL)
                .totalAmount(refund.getRefundAmount())
                .lastTransactionKey(cancelTransactionKey)
                .build();

        Payment saved = paymentRepository.save(cancelPayment);
        refund.approve();
        refund.linkPayment(saved);
        refund.complete(cancelTransactionKey);
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
