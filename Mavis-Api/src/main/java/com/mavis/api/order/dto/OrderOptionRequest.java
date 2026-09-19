package com.mavis.api.order.dto;

import com.mavis.domain.domains.order.domain.OrderOption;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderOptionRequest(
        @NotNull(message = "색상은 필수입니다.")
        String color,

        @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
        int quantity
) {
    public OrderOption toOrderOption() {
        return new OrderOption(color, quantity);
    }
}
