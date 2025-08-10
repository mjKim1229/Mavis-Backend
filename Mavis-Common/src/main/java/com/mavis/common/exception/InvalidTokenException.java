package com.mavis.common.exception;

public class InvalidTokenException extends MavisCodeException {
    public static final MavisCodeException EXCEPTION = new InvalidTokenException();

    private InvalidTokenException() {
        super(GlobalErrorCode.INVALID_TOKEN);
    }
}
