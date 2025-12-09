package com.mavis.common.exception;

import static com.mavis.common.exception.GlobalErrorCode.REFRESH_TOKEN_EXPIRED;

public class RefreshTokenExpiredException extends MavisCodeException {
    public static final MavisCodeException EXCEPTION = new RefreshTokenExpiredException();

    private RefreshTokenExpiredException() {
        super(REFRESH_TOKEN_EXPIRED);
    }
}
