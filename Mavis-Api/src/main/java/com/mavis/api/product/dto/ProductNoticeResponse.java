package com.mavis.api.product.dto;

import com.mavis.api.product.domain.ProductNotice;
import lombok.Builder;

@Builder
public record ProductNoticeResponse(
        Long productId,
        String precaution,
        String shippingInfo,
        String returnRequest,
        String returnProcess
) {
    public static ProductNoticeResponse of(ProductNotice productNotice, Long productId) {
        return ProductNoticeResponse.builder()
                .productId(productId)
                .precaution(productNotice.getPrecaution())
                .shippingInfo(productNotice.getShippingInfo())
                .returnRequest(productNotice.getReturnRequest())
                .returnProcess(productNotice.getReturnProcess())
                .build();
    }
}
