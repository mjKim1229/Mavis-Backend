package com.mavis.domain.domains.review.exception;

import com.mavis.common.dto.ErrorReason;
import com.mavis.common.exception.BaseErrorCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ReviewErrorCode implements BaseErrorCode {
    REVIEW_ORDER_USER_NOT_MATCH(400, "작성하려는 리뷰 주문정보의 사용자와 일치하지 않습니다", "REVIEW_400_1"),
    UNAUTHORIZED_REVIEW(403, "해당 리뷰에 대한 권한이 없습니다", "REVIEW_403_1"),
    REVIEW_NOT_FOUND(404, "리뷰를 찾을 수 없습니다", "REVIEW_404_1");

    private final Integer status;
    private final String message;
    private final String code;

    @Override
    public ErrorReason getErrorReason() {
        return new ErrorReason(status, code, message);
    }
}
