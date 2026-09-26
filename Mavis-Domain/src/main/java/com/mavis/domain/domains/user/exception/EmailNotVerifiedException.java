package com.mavis.domain.domains.user.exception;

import com.mavis.common.exception.MavisCodeException;

import static com.mavis.domain.domains.user.exception.UserErrorCode.EMAIL_NOT_VERIFIED;

public class EmailNotVerifiedException extends MavisCodeException {

    public static final MavisCodeException EXCEPTION = new EmailNotVerifiedException();

    private EmailNotVerifiedException() {
        super(EMAIL_NOT_VERIFIED);
    }
}
