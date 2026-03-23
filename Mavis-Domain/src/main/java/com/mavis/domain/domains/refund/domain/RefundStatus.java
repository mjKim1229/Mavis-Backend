package com.mavis.domain.domains.refund.domain;

import com.mavis.common.enums.EnumMapperType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum RefundStatus implements EnumMapperType {
    REQUESTED("환불 요청"),
    APPROVED("환불 승인"),
    REJECTED("환불 거절"),
    COMPLETED("환불 완료");

    private final String title;

    @Override
    public String getCode() {
        return name();
    }

    @Override
    public String getTitle() {
        return title;
    }
}
