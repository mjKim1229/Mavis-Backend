package com.mavis.admin.domains.order.dto;

import com.mavis.common.annotation.ExcelColumn;

import java.time.LocalDateTime;

public record GetAdminOrderExcelResponse(
        @ExcelColumn(header = "번호")
        Long rowNumber,

        @ExcelColumn(header = "주문번호")
        Long orderId,

        @ExcelColumn(header = "주문일시")
        LocalDateTime orderedAt,

        @ExcelColumn(header = "상품명")
        String productName,

        @ExcelColumn(header = "색상")
        String color,

        @ExcelColumn(header = "수량")
        int quantity,

        @ExcelColumn(header = "단가")
        int price,

        @ExcelColumn(header = "주문총액")
        int totalPrice,

        @ExcelColumn(header = "배송지")
        String address,

        @ExcelColumn(header = "우편번호")
        String postalCode,

        @ExcelColumn(header = "구매자")
        String buyerName,

        @ExcelColumn(header = "주문자 번호")
        String buyerPhoneNumber,

        @ExcelColumn(header = "수취인")
        String receiverName,

        @ExcelColumn(header = "수취인 번호")
        String receiverPhoneNumber,

        @ExcelColumn(header = "요청 사항")
        String requestMessage
) {
}