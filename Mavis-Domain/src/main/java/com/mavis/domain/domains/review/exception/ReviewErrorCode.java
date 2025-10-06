package com.mavis.domain.domains.review.exception;

import com.mavis.common.dto.ErrorReason;
import com.mavis.common.exception.BaseErrorCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ReviewErrorCode implements BaseErrorCode {
    REVIEW_ORDER_USER_NOT_MATCH(400, "작성하려는 리뷰 주문정보의 사용자와 일치하지 않습니다", "REVIEW_400_1");

    private final Integer status;
    private final String message;
    private final String code;

    @Override
    public ErrorReason getErrorReason() {
        return new ErrorReason(status, message, code);
    }
}
