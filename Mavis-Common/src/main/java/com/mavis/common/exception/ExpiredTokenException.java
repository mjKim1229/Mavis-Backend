package com.mavis.common.exception;

public class ExpiredTokenException extends MavisCodeException {
    public static final MavisCodeException EXCEPTION = new ExpiredTokenException();

    private ExpiredTokenException() {
        super(GlobalErrorCode.TOKEN_EXPIRED);
    }
}
