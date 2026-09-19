package com.mavis.api.order.dto;

import com.mavis.domain.domains.order.domain.OrderAddress;
import jakarta.validation.constraints.NotBlank;

public record OrderAddressRequest(
        @NotBlank(message = "받는 분 이름은 필수입니다.")
        String receiverName,

        @NotBlank(message = "받는 분 연락처는 필수입니다.")
        String receiverPhone,

        @NotBlank(message = "우편번호는 필수입니다.")
        String zipCode,

        @NotBlank(message = "주소는 필수입니다.")
        String address,

        @NotBlank(message = "상세주소는 필수입니다.")
        String addressDetail,

        String addressMemo
) {
    public OrderAddress toOrderAddress() {
        return new OrderAddress(receiverName, receiverPhone, zipCode, address, addressDetail, addressMemo);
    }
}
