package com.mavis.admin.domains.delivery.dto;

import com.mavis.admin.domains.order.dto.OrderItemInfo;
import com.mavis.common.util.DateFormatters;
import com.mavis.domain.domains.delivery.dto.AdminDeliveryRow;
import lombok.Builder;

import java.util.List;

import static com.mavis.common.util.OrderNumberGenerator.DOMAIN_PREFIX;

@Builder
public record GetAdminDeliveryResponse(
        Long deliveryId,
        String orderId,
        String carrier,
        String trackingNumber,
        List<OrderItemInfo> orderItemInfos,
        String receiverName,
        String receiverPhoneNumber,
        String address,
        int totalPrice,
        String deliveryStatus,
        String buyerName,
        String orderedAt,
        String requestMessage
) {
    public static GetAdminDeliveryResponse from(AdminDeliveryRow row, List<OrderItemInfo> orderItemInfos) {
        return GetAdminDeliveryResponse.builder()
                .deliveryId(row.deliveryId())
                .orderId(row.tossOrderId().substring(DOMAIN_PREFIX.length()))
                .carrier(row.carrier())
                .trackingNumber(row.trackingNumber())
                .deliveryStatus(row.deliveryStatus().getTitle())
                .orderItemInfos(orderItemInfos)
                .receiverName(row.receiverName())
                .receiverPhoneNumber(row.receiverPhone())
                .address(row.address())
                .requestMessage(row.addressMemo())
                .buyerName(row.buyerName())
                .orderedAt(row.createdAt().format(DateFormatters.DATE_FORMATTER))
                .totalPrice(row.totalPrice())
                .build();
    }
}