package com.mavis.domain.domains.user.exception;

import com.mavis.common.exception.MavisCodeException;

public class VerificationCodeExpiredException extends MavisCodeException {

    public static final MavisCodeException EXCEPTION = new VerificationCodeExpiredException();

    private VerificationCodeExpiredException() {
        super(UserErrorCode.VERIFICATION_CODE_EXPIRED);
    }
}
