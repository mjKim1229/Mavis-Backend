package com.mavis.api.cart.dto;

import com.mavis.common.enums.Size;

public record UpdateCartRequest(
        String color,
        Size size,
        int quantity
) {
}
