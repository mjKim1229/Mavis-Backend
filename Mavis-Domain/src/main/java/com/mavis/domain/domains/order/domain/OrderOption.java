package com.mavis.domain.domains.order.domain;

import com.mavis.common.enums.Size;

public record OrderOption(String color, Size size, int quantity) {

    public OrderOption(String color, int quantity) {
        this(color, null, quantity);
    }
}
