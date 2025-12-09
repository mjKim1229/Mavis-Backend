package com.mavis.domain.domains.user.exception;

import com.mavis.common.exception.MavisCodeException;

public class InvalidVerificationCodeException extends MavisCodeException {

    public static final MavisCodeException EXCEPTION = new InvalidVerificationCodeException();

    private InvalidVerificationCodeException() {
        super(UserErrorCode.INVALID_VERIFICATION_CODE);
    }
}
