package com.mavis.domain.domains.order.exception;

import com.mavis.common.exception.MavisCodeException;

public class CannotCancelOrderException extends MavisCodeException {
    public static final MavisCodeException EXCEPTION = new CannotCancelOrderException();

    private CannotCancelOrderException() {
        super(OrderErrorCode.ORDER_NOT_TO_BE_CONFIRMED);
    }
}
