package com.mavis.domain.domains.cart.exception;

import com.mavis.common.dto.ErrorReason;
import com.mavis.common.exception.BaseErrorCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CartErrorCode implements BaseErrorCode {

    CART_ITEM_NOT_FOUND(400, "장바구니가 존재하지 않습니다", "CART_400_1");

    private final Integer status;
    private final String message;
    private final String code;

    @Override
    public ErrorReason getErrorReason() {
        return new ErrorReason(status, message, code);
    }
}
