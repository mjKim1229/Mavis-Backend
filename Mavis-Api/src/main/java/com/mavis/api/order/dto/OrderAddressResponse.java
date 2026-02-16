package com.mavis.api.order.dto;

import com.mavis.domain.domains.user.domain.DeliveryAddress;
import com.mavis.domain.domains.user.domain.User;
import lombok.Builder;

@Builder
public record OrderAddressResponse(
        String receiverName,
        String receiverPhone,
        String zipCode,
        String address,
        String addressDetail
) {
    public static OrderAddressResponse from(User user) {
        DeliveryAddress defaultDeliveryAddress = user.getDefaultDeliveryAddress();
        if (defaultDeliveryAddress == null) {
            return OrderAddressResponse.builder().build();
        }
        return OrderAddressResponse.builder()
                .receiverName(defaultDeliveryAddress.getReceiverName())
                .receiverPhone(defaultDeliveryAddress.getReceiverPhone())
                .zipCode(defaultDeliveryAddress.getZipCode())
                .address(defaultDeliveryAddress.getAddress())
                .addressDetail(defaultDeliveryAddress.getAddressDetail())
                .build();
    }
}
