package com.mavis.api.refund.service;

import com.mavis.api.auth.implement.UserReader;
import com.mavis.api.refund.dto.RequestReturnRequest;
import com.mavis.api.refund.implement.RefundImageUploader;
import com.mavis.domain.domains.claim.domain.Claim;
import com.mavis.domain.domains.claim.domain.ClaimReturn;
import com.mavis.domain.domains.claim.implement.ClaimReader;
import com.mavis.domain.domains.claim.repository.ClaimRepository;
import com.mavis.domain.domains.claim.repository.ClaimReturnRepository;
import com.mavis.domain.domains.delivery.domain.Delivery;
import com.mavis.domain.domains.delivery.domain.DeliveryStatus;
import com.mavis.domain.domains.delivery.repository.DeliveryRepository;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.order.implement.OrderReader;
import com.mavis.domain.domains.refund.exception.AlreadyRefundRequestedException;
import com.mavis.domain.domains.refund.exception.CannotRefundException;
import com.mavis.domain.domains.refund.exception.UnauthorizedRefundException;
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
    private final ClaimReader claimReader;
    private final ClaimRepository claimRepository;
    private final ClaimReturnRepository claimReturnRepository;
    private final RefundImageUploader refundImageUploader;
    private final DeliveryRepository deliveryRepository;

    @Transactional
    public void createReturnRefund(Long orderItemId, RequestReturnRequest request, List<MultipartFile> images) {
        User currentUser = userReader.getCurrentUser();
        OrderItem orderItem = orderReader.findOrderItemById(orderItemId);

        Order order = orderItem.getOrder();
        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw UnauthorizedRefundException.EXCEPTION;
        }

        Delivery delivery = deliveryRepository.findByOrder(order).orElse(null);
        if (delivery == null || delivery.getDeliveryStatus() != DeliveryStatus.DELIVERED) {
            throw CannotRefundException.EXCEPTION;
        }

        if (claimReader.hasClaim(orderItem)) {
            throw AlreadyRefundRequestedException.EXCEPTION;
        }

        Claim claim = Claim.requestReturn(orderItem, request.refundReason());
        claimRepository.save(claim);

        ClaimReturn claimReturn = ClaimReturn.of(claim, request.carrier(), request.trackingNumber());
        claimReturnRepository.save(claimReturn);

        refundImageUploader.saveRefundImages(images, claim);
    }
}
