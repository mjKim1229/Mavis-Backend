package com.mavis.admin.domains.order.dto;

import com.mavis.common.annotation.ExcelColumn;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderAddress;
import com.mavis.domain.domains.user.domain.User;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

import static com.mavis.common.util.OrderNumberGenerator.DOMAIN_PREFIX;

@Builder
public record GetAdminOrderExcelResponse(
//        @ExcelColumn(header = "번호")
//        Long rowNumber,

        @ExcelColumn(header = "주문번호")
        String orderId,

        @ExcelColumn(header = "주문일시")
        String orderedAt,

        List<OrderItemExcelInfo> orderItemInfos,

        @ExcelColumn(header = "주문총액")
        int totalPrice,

        @ExcelColumn(header = "배송지")
        String address,

        @ExcelColumn(header = "우편번호")
        String zipCode,

        @ExcelColumn(header = "구매자")
        String buyerName,

        @ExcelColumn(header = "수취인")
        String receiverName,

        @ExcelColumn(header = "수취인 번호")
        String receiverPhoneNumber,

        @ExcelColumn(header = "요청 사항")
        String requestMessage
) {
    public static GetAdminOrderExcelResponse from(Order order, OrderAddress orderAddress, List<OrderItemExcelInfo> orderItemInfos, User user, String orderedAt) {
        return GetAdminOrderExcelResponse.builder()
                .orderItemInfos(orderItemInfos)
                .orderId(order.getOrderId().substring(DOMAIN_PREFIX.length()))
                .orderedAt(orderedAt)
                .totalPrice(order.getTotalPrice())
                .address(orderAddress.getAddress())
                .zipCode(orderAddress.getZipCode())
                .receiverName(orderAddress.getReceiverName())
                .receiverPhoneNumber(orderAddress.getReceiverPhone())
                .requestMessage(orderAddress.getAddressMemo())
                .buyerName(user.getUsername())
                .build();
    }
}