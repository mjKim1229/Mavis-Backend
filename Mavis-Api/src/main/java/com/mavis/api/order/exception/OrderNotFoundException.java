package com.mavis.api.order.exception;

import com.mavis.common.exception.MavisCodeException;

public class OrderNotFoundException extends MavisCodeException {
    public static final MavisCodeException EXCEPTION = new OrderNotFoundException();

    private OrderNotFoundException() {
        super(OrderErrorCode.ORDER_NOT_FOUND);
    }
}
