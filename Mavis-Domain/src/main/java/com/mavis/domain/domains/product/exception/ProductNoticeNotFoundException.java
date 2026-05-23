package com.mavis.domain.domains.product.exception;

import com.mavis.common.exception.MavisCodeException;

public class ProductNoticeNotFoundException extends MavisCodeException {
    public static final MavisCodeException EXCEPTION = new ProductNoticeNotFoundException();

    private ProductNoticeNotFoundException() {
        super(ProductErrorCode.PRODUCT_NOTICE_NOT_FOUND);
    }
}
