package com.mavis.domain.domains.order.exception;

import com.mavis.common.exception.MavisCodeException;

public class CannotCancelOrderException extends MavisCodeException {
    public static final MavisCodeException EXCEPTION = new CannotCancelOrderException();

    private CannotCancelOrderException() {
        super(OrderErrorCode.CANNOT_CANCEL_ORDER);
    }
}
