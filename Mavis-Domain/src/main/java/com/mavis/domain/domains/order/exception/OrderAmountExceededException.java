package com.mavis.domain.domains.order.exception;

import com.mavis.common.exception.MavisCodeException;

import static com.mavis.domain.domains.order.exception.OrderErrorCode.ORDER_AMOUNT_EXCEEDED;

public class OrderAmountExceededException extends MavisCodeException {

    public static final MavisCodeException EXCEPTION = new OrderAmountExceededException();

    private OrderAmountExceededException() {
        super(ORDER_AMOUNT_EXCEEDED);
    }
}
