package com.mavis.admin.product.dto;

public record ProductNoticeVO(
        String precaution,
        String shippingInfo,
        String returnRequest,
        String returnProcess
) {
}
