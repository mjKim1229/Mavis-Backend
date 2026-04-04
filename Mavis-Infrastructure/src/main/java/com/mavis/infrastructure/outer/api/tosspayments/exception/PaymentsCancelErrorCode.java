package com.mavis.infrastructure.outer.api.tosspayments.exception;

import com.mavis.common.dto.ErrorReason;
import com.mavis.common.exception.BaseErrorCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PaymentsCancelErrorCode implements BaseErrorCode {

    // 400
    ALREADY_CANCELED_PAYMENT(400, "이미 취소된 결제 입니다.", "PAYMENTS_CANCEL_400_1"),
    INVALID_REFUND_ACCOUNT_INFO(400, "환불 계좌번호와 예금주명이 일치하지 않습니다.", "PAYMENTS_CANCEL_400_2"),
    EXCEED_CANCEL_AMOUNT_DISCOUNT_AMOUNT(400, "즉시할인금액보다 적은 금액은 부분취소가 불가능합니다.", "PAYMENTS_CANCEL_400_3"),
    INVALID_REQUEST(400, "잘못된 요청입니다.", "PAYMENTS_CANCEL_400_4"),
    INVALID_REFUND_ACCOUNT_NUMBER(400, "잘못된 환불 계좌번호입니다.", "PAYMENTS_CANCEL_400_5"),
    INVALID_BANK(400, "유효하지 않은 은행입니다.", "PAYMENTS_CANCEL_400_6"),
    NOT_MATCHES_REFUNDABLE_AMOUNT(400, "잔액 결과가 일치하지 않습니다.", "PAYMENTS_CANCEL_400_7"),
    PROVIDER_ERROR(400, "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", "PAYMENTS_CANCEL_400_8"),
    REFUND_REJECTED(400, "환불이 거절됐습니다. 결제사에 문의 부탁드립니다.", "PAYMENTS_CANCEL_400_9"),
    ALREADY_REFUND_PAYMENT(400, "이미 환불된 결제입니다.", "PAYMENTS_CANCEL_400_10"),
    FORBIDDEN_BANK_REFUND_REQUEST(400, "고객 계좌가 입금이 되지 않는 상태입니다.", "PAYMENTS_CANCEL_400_11");

    private final Integer status;
    private final String message;
    private final String code;

    @Override
    public ErrorReason getErrorReason() {
        return new ErrorReason(status, message, code);
    }
}
