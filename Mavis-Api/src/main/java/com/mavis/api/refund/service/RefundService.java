package com.mavis.api.refund.service;

import com.mavis.api.auth.implement.UserReader;
import com.mavis.api.refund.dto.CreateRefundRequest;
import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.order.implement.OrderReader;
import com.mavis.domain.domains.refund.domain.Refund;
import com.mavis.domain.domains.refund.exception.AlreadyRefundRequestedException;
import com.mavis.domain.domains.refund.exception.InvalidRefundQuantityException;
import com.mavis.domain.domains.refund.implement.RefundAppender;
import com.mavis.domain.domains.refund.implement.RefundReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefundService {

    private final UserReader userReader;
    private final OrderReader orderReader;
    private final RefundReader refundReader;
    private final RefundAppender refundAppender;

    @Transactional
    public Refund createRefund(Long orderItemId, CreateRefundRequest request) {
        OrderItem orderItem = orderReader.findOrderItemById(orderItemId);

        if (refundReader.hasActiveRefund(orderItem)) {
            throw AlreadyRefundRequestedException.EXCEPTION;
        }

        if (request.refundQuantity() <= 0 || request.refundQuantity() > orderItem.getQuantity()) {
            throw InvalidRefundQuantityException.EXCEPTION;
        }

        int refundAmount = orderItem.getPrice() * request.refundQuantity();

        Refund refund = Refund.builder()
                .orderItem(orderItem)
                .refundReason(request.refundReason())
                .refundQuantity(request.refundQuantity())
                .refundAmount(refundAmount)
                .build();

        return refundAppender.save(refund);
    }

    @Transactional
    public void completeRefund(Refund refund, String cancelTransactionKey) {
        refund.complete(cancelTransactionKey);
    }
}
