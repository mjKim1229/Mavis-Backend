package com.mavis.admin.domains.order.dto;

import com.mavis.common.annotation.ExcelColumn;
import lombok.Builder;

@Builder
public record OrderItemInfo(
        @ExcelColumn(header = "상품명")
        String productName,

        @ExcelColumn(header = "색상")
        String color,

        @ExcelColumn(header = "수량")
        int quantity
) {
}
