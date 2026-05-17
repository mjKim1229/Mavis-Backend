package com.mavis.domain.domains.user.exception;

import com.mavis.common.exception.MavisCodeException;

public class DuplicateEmailException extends MavisCodeException {

    public static final MavisCodeException EXCEPTION = new DuplicateEmailException();

    private DuplicateEmailException() {
        super(UserErrorCode.DUPLICATE_EMAIL);
    }
}
