package com.mavis.domain.domains.favorite.exception;

import com.mavis.common.exception.MavisCodeException;

public class FavoriteAlreadyExistsException extends MavisCodeException {

    public static final MavisCodeException EXCEPTION = new FavoriteAlreadyExistsException();

    public FavoriteAlreadyExistsException() {
        super(FavoriteErrorCode.FAVORITE_ALREADY_EXISTS);
    }
}
