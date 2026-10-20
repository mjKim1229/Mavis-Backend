package com.mavis.domain.domains.order.exception;

import com.mavis.common.exception.MavisCodeException;

import static com.mavis.domain.domains.order.exception.OrderErrorCode.INVALID_ORDER_SIZE;

public class InvalidOrderSizeException extends MavisCodeException {

    public static final MavisCodeException EXCEPTION = new InvalidOrderSizeException();

    private InvalidOrderSizeException() {
        super(INVALID_ORDER_SIZE);
    }
}
