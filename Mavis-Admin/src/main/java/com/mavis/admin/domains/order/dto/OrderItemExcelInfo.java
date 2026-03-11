package com.mavis.admin.domains.order.dto;

import com.mavis.common.annotation.ExcelColumn;
import lombok.Builder;

@Builder
public record OrderItemExcelInfo(
        @ExcelColumn(header = "상품명")
        String productName,

        @ExcelColumn(header = "옵션/수량")
        String optionQuantity
) {
    public static OrderItemExcelInfo from(String productName, String color, int quantity) {
        return OrderItemExcelInfo.builder()
                .productName(productName)
                .optionQuantity(color + "/" + quantity)
                .build();
    }
}
