package com.mavis.domain.domains.order.exception;

import com.mavis.common.exception.MavisCodeException;

import static com.mavis.domain.domains.order.exception.OrderErrorCode.INVALID_ORDER_QUANTITY;

public class InvalidOrderQuantityException extends MavisCodeException {

    public static final MavisCodeException EXCEPTION = new InvalidOrderQuantityException();

    private InvalidOrderQuantityException() {
        super(INVALID_ORDER_QUANTITY);
    }
}
