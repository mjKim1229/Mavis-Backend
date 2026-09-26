package com.mavis.domain.domains.user.exception;

import com.mavis.common.exception.MavisCodeException;

public class DuplicateUsernameException extends MavisCodeException {

    public static final MavisCodeException EXCEPTION = new DuplicateUsernameException();

    private DuplicateUsernameException() {
        super(UserErrorCode.DUPLICATE_USERNAME);
    }
}
