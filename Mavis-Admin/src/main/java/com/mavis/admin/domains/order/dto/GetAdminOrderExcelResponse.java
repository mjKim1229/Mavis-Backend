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
        String tossOrderId,

        List<OrderItemExcelInfo> orderItemInfos,

        @ExcelColumn(header = "수취인")
        String receiverName,

        @ExcelColumn(header = "수취인 연락처")
        String receiverPhoneNumber,

        @ExcelColumn(header = "배송지")
        String address,

        @ExcelColumn(header = "주문자명")
        String buyerName,

        @ExcelColumn(header = "주문일시")
        String orderedAt,

        @ExcelColumn(header = "주문총액")
        int totalPrice,

        @ExcelColumn(header = "요청 사항")
        String requestMessage
) {
    public static GetAdminOrderExcelResponse from(Order order, OrderAddress orderAddress, List<OrderItemExcelInfo> orderItemInfos, User user, String orderedAt) {
        return GetAdminOrderExcelResponse.builder()
                .orderItemInfos(orderItemInfos)
                .tossOrderId(order.getOrderId().substring(DOMAIN_PREFIX.length()))
                .orderedAt(orderedAt)
                .totalPrice(order.getTotalPrice())
                .address(orderAddress.getAddress())
                .receiverName(orderAddress.getReceiverName())
                .receiverPhoneNumber(orderAddress.getReceiverPhone())
                .requestMessage(orderAddress.getAddressMemo())
                .buyerName(user.getName())
                .build();
    }
}