package com.mavis.domain.domains.order.exception;

import com.mavis.common.exception.MavisCodeException;

public class OrderNotToBeConfirmedException extends MavisCodeException {

    public static final MavisCodeException EXCEPTION = new OrderNotToBeConfirmedException();

    private OrderNotToBeConfirmedException() {
        super(OrderErrorCode.ORDER_NOT_TO_BE_CONFIRMED);
    }
}
