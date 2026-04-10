package com.mavis.domain.domains.favorite.exception;

import com.mavis.common.exception.MavisCodeException;

public class UnauthorizedFavoriteException extends MavisCodeException {
    public static final MavisCodeException EXCEPTION = new UnauthorizedFavoriteException();

    private UnauthorizedFavoriteException() {
        super(FavoriteErrorCode.UNAUTHORIZED_FAVORITE);
    }
}
