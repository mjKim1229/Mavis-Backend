package com.mavis.api.refund.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record GetReturnPreviewRequest(
        @NotEmpty(message = "반품할 상품은 필수입니다.")
        @Size(max = 50, message = "한 번에 반품할 수 있는 상품은 최대 50개입니다.")
        List<Long> orderItemIds
) {
}
