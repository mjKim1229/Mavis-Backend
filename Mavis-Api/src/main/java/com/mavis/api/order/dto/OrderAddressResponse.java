package com.mavis.api.order.dto;

import com.mavis.domain.domains.user.domain.User;
import lombok.Builder;

@Builder
public record OrderAddressResponse(
        String defaultAddress
) {
    public static OrderAddressResponse from(User user) {
        return OrderAddressResponse.builder()
                .defaultAddress(user.getDefaultAddress())
                .build();
    }
}
