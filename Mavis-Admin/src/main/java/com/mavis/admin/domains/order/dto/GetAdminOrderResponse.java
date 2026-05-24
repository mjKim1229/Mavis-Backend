package com.mavis.admin.domains.order.dto;

import com.mavis.common.util.DateFormatters;
import com.mavis.domain.domains.order.dto.AdminOrderRow;
import lombok.Builder;

import java.util.List;

import static com.mavis.common.util.OrderNumberGenerator.DOMAIN_PREFIX;

@Builder
public record GetAdminOrderResponse(
        Long orderId,
        String tossOrderId,
        List<OrderItemInfo> orderItemInfos,
        String receiverName,
        String receiverPhoneNumber,
        String address,
        String buyerName,
        String orderedAt,
        int totalPrice,
        String requestMessage
) {
    public static GetAdminOrderResponse from(AdminOrderRow row, List<OrderItemInfo> orderItemInfos) {
        return GetAdminOrderResponse.builder()
                .orderItemInfos(orderItemInfos)
                .orderId(row.orderId())
                .tossOrderId(row.tossOrderId().substring(DOMAIN_PREFIX.length()))
                .orderedAt(row.createdAt().format(DateFormatters.DATE_FORMATTER))
                .totalPrice(row.totalPrice())
                .address(row.address())
                .receiverName(row.receiverName())
                .receiverPhoneNumber(row.receiverPhone())
                .requestMessage(row.addressMemo())
                .buyerName(row.buyerName())
                .build();
    }
}
