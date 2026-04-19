package com.mavis.api.refund.service;

import com.mavis.api.auth.implement.UserReader;
import com.mavis.api.refund.dto.RequestReturnRequest;
import com.mavis.api.refund.implement.RefundImageUploader;
import com.mavis.domain.domains.delivery.domain.Delivery;
import com.mavis.domain.domains.delivery.domain.DeliveryStatus;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.order.implement.OrderReader;
import com.mavis.domain.domains.refund.domain.Refund;
import com.mavis.domain.domains.refund.domain.RefundType;
import com.mavis.domain.domains.refund.exception.AlreadyRefundRequestedException;
import com.mavis.domain.domains.refund.exception.CannotRefundException;
import com.mavis.domain.domains.refund.exception.UnauthorizedRefundException;
import com.mavis.domain.domains.refund.implement.RefundAppender;
import com.mavis.domain.domains.refund.implement.RefundReader;
import com.mavis.domain.domains.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RefundService {

    private final UserReader userReader;
    private final OrderReader orderReader;
    private final RefundReader refundReader;
    private final RefundAppender refundAppender;
    private final RefundImageUploader refundImageUploader;

    @Transactional
    public void createReturnRefund(Long orderItemId, RequestReturnRequest request, List<MultipartFile> images) {
        User currentUser = userReader.getCurrentUser();
        OrderItem orderItem = orderReader.findOrderItemById(orderItemId);

        Order order = orderItem.getOrder();
        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw UnauthorizedRefundException.EXCEPTION;
        }

        Delivery delivery = order.getDelivery();
        if (delivery == null || delivery.getDeliveryStatus() != DeliveryStatus.DELIVERED) {
            throw CannotRefundException.EXCEPTION;
        }

        if (refundReader.hasActiveRefund(orderItem)) {
            throw AlreadyRefundRequestedException.EXCEPTION;
        }

        int refundAmount = orderItem.getPrice() * orderItem.getQuantity();

        Refund refund = Refund.builder()
                .orderItem(orderItem)
                .refundReason(request.refundReason())
                .refundAmount(refundAmount)
                .carrier(request.carrier())
                .trackingNumber(request.trackingNumber())
                .refundType(RefundType.RETURN)
                .build();

        Refund saved = refundAppender.save(refund);
        refundImageUploader.saveRefundImages(images, saved);
    }

    @Transactional
    public void completeRefund(Refund refund, String cancelTransactionKey) {
        refund.complete(cancelTransactionKey);
    }
}
