package com.mavis.domain.domains.order.exception;

import com.mavis.common.dto.ErrorReason;
import com.mavis.common.exception.BaseErrorCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum OrderErrorCode implements BaseErrorCode {
    ORDER_NOT_FOUND(404, "주문 정보를 찾을 수 없습니다", "ORDER_404_1"),
    ORDER_NOT_TO_BE_CONFIRMED(400, "주문 상태를 확인해주세요", "ORDER_400_1"),
    INVALID_ORDER_INFO(400, "올바르지 않은 결제 정보입니다.", "ORDER_400_3"),
    PRICE_MISMATCH(400, "주문 금액이 일치하지 않습니다.", "ORDER_400_2"),
    UNSUPPORTED_PAYMENT_METHOD(400, "지원하지 않는 결제 수단입니다.", "ORDER_400_4"),
    CANNOT_CANCEL_ORDER(400, "주문 취소가 불가능한 상태입니다. 고객센터에 문의하세요", "ORDER_400_5");

    private final Integer status;
    private final String message;
    private final String code;

    @Override
    public ErrorReason getErrorReason() {
        return new ErrorReason(status, message, code);
    }
}
