package com.mavis.domain.domains.user.exception;


import com.mavis.common.exception.MavisCodeException;

public class UserNotFoundException extends MavisCodeException {

    public static final MavisCodeException EXCEPTION = new UserNotFoundException();

    private UserNotFoundException() {
        super(UserErrorCode.USER_NOT_FOUND);
    }
}
