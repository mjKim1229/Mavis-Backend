package com.mavis.domain.domains.product.exception;

import com.mavis.common.exception.MavisCodeException;

public class MainImageRequiredException extends MavisCodeException {
    public static final MavisCodeException EXCEPTION = new MainImageRequiredException();

    private MainImageRequiredException() {
        super(ProductErrorCode.MAIN_IMAGE_REQUIRED);
    }
}
