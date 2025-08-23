package com.mavis.api.product.exception;

import com.mavis.common.dto.ErrorReason;
import com.mavis.common.exception.BaseErrorCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ProductErrorCode implements BaseErrorCode {
    PRODUCT_NOT_FOUND(404, "상품 정보를 찾을 수 없습니다", "PRODUCT_404_1");
    private final Integer status;
    private final String message;
    private final String code;

    @Override
    public ErrorReason getErrorReason() {
        return new ErrorReason(status, message, code);
    }
}
