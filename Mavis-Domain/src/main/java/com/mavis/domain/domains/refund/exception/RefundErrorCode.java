package com.mavis.domain.domains.refund.exception;

import com.mavis.common.dto.ErrorReason;
import com.mavis.common.exception.BaseErrorCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum RefundErrorCode implements BaseErrorCode {
    REFUND_NOT_FOUND(404, "환불 정보를 찾을 수 없습니다.", "REFUND_404_1"),
    CANNOT_REFUND(400, "환불이 불가능한 상태입니다.", "REFUND_400_1"),
    ALREADY_REFUND_REQUESTED(409, "이미 환불 요청된 주문 상품입니다.", "REFUND_409_1"),
    INVALID_REFUND_QUANTITY(400, "환불 수량이 올바르지 않습니다.", "REFUND_400_2"),
    UNAUTHORIZED_REFUND(403, "본인의 주문에 대해서만 반품 신청이 가능합니다.", "REFUND_403_1");

    private final Integer status;
    private final String message;
    private final String code;

    @Override
    public ErrorReason getErrorReason() {
        return new ErrorReason(status, message, code);
    }
}
