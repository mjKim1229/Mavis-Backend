package com.mavis.domain.domains.user.exception;

import com.mavis.common.exception.MavisCodeException;

public class InvalidPasswordException extends MavisCodeException {

    public static final MavisCodeException EXCEPTION = new InvalidPasswordException();

    private InvalidPasswordException() {
        super(UserErrorCode.INVALID_PASSWORD);
    }
}
