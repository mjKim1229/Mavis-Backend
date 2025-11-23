package com.mavis.api.order.dto;

import com.mavis.domain.domains.order.domain.OrderAddress;

public record OrderAddressRequest(
        String address,
        String addressMemo
) {
    public OrderAddress toOrderAddress() {
        return new OrderAddress(address, addressMemo);
    }
}
