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
    FORBIDDEN_BANK_REFUND_REQUEST(400, "고객 계좌가 입금이 되지 않는 상태입니다.", "PAYMENTS_CANCEL_400_11"),

    // 403
    NOT_CANCELABLE_AMOUNT(403, "취소 할 수 없는 금액 입니다.", "PAYMENTS_CANCEL_403_1"),
    FORBIDDEN_CONSECUTIVE_REQUEST(403, "반복적인 요청은 허용되지 않습니다. 잠시 후 다시 시도해주세요.", "PAYMENTS_CANCEL_403_2"),
    NOT_CANCELABLE_PAYMENT(403, "취소 할 수 없는 결제 입니다.", "PAYMENTS_CANCEL_403_3"),
    EXCEED_MAX_REFUND_DUE(403, "환불 가능한 기간이 지났습니다.", "PAYMENTS_CANCEL_403_4"),
    NOT_ALLOWED_PARTIAL_REFUND_WAITING_DEPOSIT(403, "입금 대기중인 결제는 부분 환불이 불가합니다.", "PAYMENTS_CANCEL_403_5"),
    NOT_ALLOWED_PARTIAL_REFUND(403, "에스크로 주문, 현금 카드 결제일 때는 부분 환불이 불가합니다. 이외 다른 결제 수단에서 부분 취소가 되지 않을 때는 토스페이먼츠에 문의해 주세요.", "PAYMENTS_CANCEL_403_6"),
    NOT_AVAILABLE_BANK(403, "은행 서비스 시간이 아닙니다.", "PAYMENTS_CANCEL_403_7"),
    NOT_CANCELABLE_PAYMENT_FOR_DORMANT_USER(403, "휴면 처리된 회원의 결제는 취소할 수 없습니다.", "PAYMENTS_CANCEL_403_8"),
    EXCEED_CANCEL_LIMIT(403, "취소 한도 금액을 초과 하였습니다.", "PAYMENTS_CANCEL_403_9"),

    // 404
    NOT_FOUND_PAYMENT(404, "존재하지 않는 결제 정보 입니다.", "PAYMENTS_CANCEL_404_1"),

    // 500
    FAILED_REFUND_PROCESS(500, "은행 응답시간 지연이나 일시적인 오류로 환불요청에 실패했습니다.", "PAYMENTS_CANCEL_500_1"),
    FAILED_METHOD_HANDLING_CANCEL(500, "취소 중 결제 시 사용한 결제 수단 처리과정에서 일시적인 오류가 발생했습니다.", "PAYMENTS_CANCEL_500_2"),
    FAILED_PARTIAL_REFUND(500, "은행 점검, 해약 계좌 등의 사유로 부분 환불이 실패했습니다.", "PAYMENTS_CANCEL_500_3"),
    COMMON_ERROR(500, "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", "PAYMENTS_CANCEL_500_4"),
    FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING(500, "결제가 완료되지 않았어요. 다시 시도해주세요.", "PAYMENTS_CANCEL_500_5");

    private final Integer status;
    private final String message;
    private final String code;

    @Override
    public ErrorReason getErrorReason() {
        return new ErrorReason(status, message, code);
    }
}
