package com.mavis.api.auth.dto;

import com.mavis.domain.domains.user.domain.DefaultDeliveryAddress;

public record UserAddressUpdateRequest(
        String receiverName,
        String receiverPhone,
        String zipCode,
        String address,
        String addressDetail
) {
    public DefaultDeliveryAddress toDefaultDeliveryAddress() {
        return new DefaultDeliveryAddress(receiverName, receiverPhone, zipCode, address, addressDetail);
    }
}
