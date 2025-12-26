package com.mavis.domain.domains.favorite.exception;

import com.mavis.common.exception.MavisCodeException;

public class FavoriteNotFoundException extends MavisCodeException {

    public static final MavisCodeException EXCEPTION = new FavoriteNotFoundException();

    public FavoriteNotFoundException() {
        super(FavoriteErrorCode.FAVORITE_NOT_FOUND);
    }
}
