package com.mavis.api.refund.dto;

import jakarta.validation.constraints.NotBlank;

public record RequestReturnRequest(
        String refundReason,
        @NotBlank(message = "택배사는 필수입니다.")
        String carrier,
        @NotBlank(message = "송장번호는 필수입니다.")
        String trackingNumber
) {
}
