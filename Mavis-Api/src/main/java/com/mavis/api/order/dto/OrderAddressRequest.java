package com.mavis.api.order.dto;

import com.mavis.domain.domains.order.domain.OrderAddress;

public record OrderAddressRequest(
        String receiverName,
        String receiverPhone,
        String zipCode,
        String address,
        String addressDetail,
        String addressMemo
) {
    public OrderAddress toOrderAddress() {
        return new OrderAddress(receiverName, receiverPhone, zipCode, address, addressDetail, addressMemo);
    }
}
