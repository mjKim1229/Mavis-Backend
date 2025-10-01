package com.mavis.domain.domains.admin.exception;

import com.mavis.common.exception.MavisCodeException;
import com.mavis.domain.domains.cart.exception.CartErrorCode;

public class CartItemNotFoundException extends MavisCodeException {
    public static MavisCodeException EXCEPTION = new CartItemNotFoundException();

    private CartItemNotFoundException() {
        super(CartErrorCode.CART_ITEM_NOT_FOUND);
    }
}
