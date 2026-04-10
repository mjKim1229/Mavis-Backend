package com.mavis.domain.domains.cart.exception;

import com.mavis.common.exception.MavisCodeException;

public class UnauthorizedCartException extends MavisCodeException {
    public static final MavisCodeException EXCEPTION = new UnauthorizedCartException();

    private UnauthorizedCartException() {
        super(CartErrorCode.UNAUTHORIZED_CART);
    }
}
