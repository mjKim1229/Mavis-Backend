package com.mavis.api.refund.service;

import com.mavis.api.auth.implement.UserReader;
import com.mavis.api.refund.dto.RequestReturnRequest;
import com.mavis.api.refund.implement.RefundImageUploader;
import com.mavis.domain.domains.claim.domain.Claim;
import com.mavis.domain.domains.claim.domain.ReturnItemReason;
import com.mavis.domain.domains.claim.implement.ClaimReader;
import com.mavis.domain.domains.claim.repository.ClaimRepository;
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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RefundService {

    private final UserReader userReader;
    private final OrderReader orderReader;
    private final ClaimReader claimReader;
    private final ClaimRepository claimRepository;
    private final RefundImageUploader refundImageUploader;
    private final DeliveryRepository deliveryRepository;

    @Transactional
    public void createReturnRefund(RequestReturnRequest request, List<MultipartFile> images) {
        User currentUser = userReader.getCurrentUser();
        List<Long> orderItemIds = request.orderItemIds();
        Set<Long> distinctIds = new HashSet<>(orderItemIds);
        if (distinctIds.size() != orderItemIds.size()) {
            throw CannotRefundException.EXCEPTION;
        }
        List<OrderItem> orderItems = orderReader.findOrderItemsByIds(orderItemIds);
        OrderItem firstItem = orderItems.get(0);
        Order order = firstItem.getOrder();

        Long orderId = order.getId();
        boolean sameOrder = orderItems.stream()
            .map(OrderItem::getOrder)
            .allMatch(itemOrder -> itemOrder.getId().equals(orderId));
        if (!sameOrder) {
            throw CannotRefundException.EXCEPTION;
        }

        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw UnauthorizedRefundException.EXCEPTION;
        }

        Delivery delivery = deliveryRepository.findByOrder(order).orElse(null);
        if (delivery == null || delivery.getDeliveryStatus() != DeliveryStatus.DELIVERED) {
            throw CannotRefundException.EXCEPTION;
        }

        if (claimReader.hasAnyClaim(orderItems)) {
            throw AlreadyRefundRequestedException.EXCEPTION;
        }

        Map<Long, String> reasonByItemId = request.reasonByOrderItemId();
        List<ReturnItemReason> returnItems = orderItems.stream()
            .map(orderItem -> new ReturnItemReason(orderItem, reasonByItemId.get(orderItem.getId())))
            .toList();

        Claim claim = Claim.requestReturn(order, request.reasonType(), returnItems);
        claimRepository.save(claim);

        refundImageUploader.saveRefundImages(images, claim);
    }
}
