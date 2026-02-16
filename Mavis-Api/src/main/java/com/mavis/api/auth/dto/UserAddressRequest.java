package com.mavis.api.auth.dto;

import com.mavis.domain.domains.user.domain.DeliveryAddress;

public record UserAddressRequest(
        String receiverName,
        String receiverPhone,
        String zipCode,
        String address,
        String addressDetail
) {
    public DeliveryAddress toDefaultDeliveryAddress() {
        return new DeliveryAddress(receiverName, receiverPhone, zipCode, address, addressDetail);
    }
}
