package com.mavis.domain.domains.order.exception;

import com.mavis.common.exception.MavisCodeException;

import static com.mavis.domain.domains.order.exception.OrderErrorCode.INVALID_ORDER_INFO;

public class InvalidOrderInfoException extends MavisCodeException {

    public static final MavisCodeException EXCEPTION = new InvalidOrderInfoException();

    private InvalidOrderInfoException() {
        super(INVALID_ORDER_INFO);
    }
}
