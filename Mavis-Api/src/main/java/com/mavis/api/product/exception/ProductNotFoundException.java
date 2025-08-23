package com.mavis.api.product.exception;

import com.mavis.common.exception.MavisCodeException;

public class ProductNotFoundException extends MavisCodeException {
    public static final MavisCodeException EXCEPTION = new ProductNotFoundException();

    private ProductNotFoundException() {
        super(ProductErrorCode.PRODUCT_NOT_FOUND);
    }
}
