package com.mavis.admin.domains.product.dto;

public record ProductNoticeVO(
        String precaution,
        String shippingInfo,
        String returnRequest,
        String returnProcess
) {
}
