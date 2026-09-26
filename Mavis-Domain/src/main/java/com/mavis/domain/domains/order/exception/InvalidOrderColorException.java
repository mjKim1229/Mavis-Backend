package com.mavis.domain.domains.order.exception;

import com.mavis.common.exception.MavisCodeException;

import static com.mavis.domain.domains.order.exception.OrderErrorCode.INVALID_ORDER_COLOR;

public class InvalidOrderColorException extends MavisCodeException {

    public static final MavisCodeException EXCEPTION = new InvalidOrderColorException();

    private InvalidOrderColorException() {
        super(INVALID_ORDER_COLOR);
    }
}
