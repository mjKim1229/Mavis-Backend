package com.mavis.domain.domains.order.exception;

import com.mavis.common.exception.MavisCodeException;

import static com.mavis.domain.domains.order.exception.OrderErrorCode.PRICE_MISMATCH;

public class PriceMismatchException extends MavisCodeException {

    public static final MavisCodeException EXCEPTION = new PriceMismatchException();

    private PriceMismatchException() {
        super(PRICE_MISMATCH);
    }
}
