package com.mavis.domain.domains.order.exception;

import com.mavis.common.exception.MavisCodeException;

import static com.mavis.domain.domains.order.exception.OrderErrorCode.INVALID_ORDER_STATUS_TRANSITION;

public class InvalidOrderStatusTransitionException extends MavisCodeException {

    public static final MavisCodeException EXCEPTION = new InvalidOrderStatusTransitionException();

    private InvalidOrderStatusTransitionException() {
        super(INVALID_ORDER_STATUS_TRANSITION);
    }
}
