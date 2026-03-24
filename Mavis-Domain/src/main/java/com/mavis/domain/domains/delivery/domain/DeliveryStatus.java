package com.mavis.domain.domains.delivery.domain;

import com.mavis.common.enums.EnumMapperType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum DeliveryStatus implements EnumMapperType {
    READY("배송 준비"), //발주 완료
    SHIPPED("배송중"),
    DELIVERED("배송 완료"),
    CANCELLED("배송 취소");

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
