package com.mavis.infrastructure.outer.api.tosspayments.exception;

import com.mavis.common.dto.ErrorReason;
import com.mavis.common.exception.BaseErrorCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PaymentsConfirmErrorCode implements BaseErrorCode {

    // 400
    ALREADY_PROCESSED_PAYMENT(400, "이미 처리된 결제 입니다.", "PAYMENTS_CONFIRM_400_1"),
    PROVIDER_ERROR(400, "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", "PAYMENTS_CONFIRM_400_2"),
    EXCEED_MAX_CARD_INSTALLMENT_PLAN(400, "설정 가능한 최대 할부 개월 수를 초과했습니다.", "PAYMENTS_CONFIRM_400_3"),
    INVALID_REQUEST(400, "잘못된 요청입니다.", "PAYMENTS_CONFIRM_400_4"),
    NOT_ALLOWED_POINT_USE(400, "포인트 사용이 불가한 카드로 카드 포인트 결제에 실패했습니다.", "PAYMENTS_CONFIRM_400_5"),
    INVALID_API_KEY(400, "잘못된 시크릿키 연동 정보 입니다.", "PAYMENTS_CONFIRM_400_6"),
    INVALID_REJECT_CARD(400, "카드 사용이 거절되었습니다. 카드사 문의가 필요합니다.", "PAYMENTS_CONFIRM_400_7"),
    BELOW_MINIMUM_AMOUNT(400, "신용카드는 결제금액이 100원 이상, 계좌는 200원이상부터 결제가 가능합니다.", "PAYMENTS_CONFIRM_400_8"),
    INVALID_CARD_EXPIRATION(400, "카드 정보를 다시 확인해주세요. (유효기간)", "PAYMENTS_CONFIRM_400_9"),
    INVALID_STOPPED_CARD(400, "정지된 카드 입니다.", "PAYMENTS_CONFIRM_400_10"),
    EXCEED_MAX_DAILY_PAYMENT_COUNT(400, "하루 결제 가능 횟수를 초과했습니다.", "PAYMENTS_CONFIRM_400_11"),
    NOT_SUPPORTED_INSTALLMENT_PLAN_CARD_OR_MERCHANT(400, "할부가 지원되지 않는 카드 또는 가맹점 입니다.", "PAYMENTS_CONFIRM_400_12"),
    INVALID_CARD_INSTALLMENT_PLAN(400, "할부 개월 정보가 잘못되었습니다.", "PAYMENTS_CONFIRM_400_13"),
    NOT_SUPPORTED_MONTHLY_INSTALLMENT_PLAN(400, "할부가 지원되지 않는 카드입니다.", "PAYMENTS_CONFIRM_400_14"),
    EXCEED_MAX_PAYMENT_AMOUNT(400, "하루 결제 가능 금액을 초과했습니다.", "PAYMENTS_CONFIRM_400_15"),
    NOT_FOUND_TERMINAL_ID(400, "단말기번호(Terminal Id)가 없습니다. 토스페이먼츠로 문의 바랍니다.", "PAYMENTS_CONFIRM_400_16"),
    INVALID_AUTHORIZE_AUTH(400, "유효하지 않은 인증 방식입니다.", "PAYMENTS_CONFIRM_400_17"),
    INVALID_CARD_LOST_OR_STOLEN(400, "분실 혹은 도난 카드입니다.", "PAYMENTS_CONFIRM_400_18"),
    RESTRICTED_TRANSFER_ACCOUNT(400, "계좌는 등록 후 12시간 뒤부터 결제할 수 있습니다. 관련 정책은 해당 은행으로 문의해주세요.", "PAYMENTS_CONFIRM_400_19"),
    INVALID_CARD_NUMBER(400, "카드번호를 다시 확인해주세요.", "PAYMENTS_CONFIRM_400_20"),
    INVALID_UNREGISTERED_SUBMALL(400, "등록되지 않은 서브몰입니다. 서브몰이 없는 가맹점이라면 안심클릭이나 ISP 결제가 필요합니다.", "PAYMENTS_CONFIRM_400_21"),
    NOT_REGISTERED_BUSINESS(400, "등록되지 않은 사업자 번호입니다.", "PAYMENTS_CONFIRM_400_22"),
    EXCEED_MAX_ONE_DAY_WITHDRAW_AMOUNT(400, "1일 출금 한도를 초과했습니다.", "PAYMENTS_CONFIRM_400_23"),
    EXCEED_MAX_ONE_TIME_WITHDRAW_AMOUNT(400, "1회 출금 한도를 초과했습니다.", "PAYMENTS_CONFIRM_400_24"),
    CARD_PROCESSING_ERROR(400, "카드사에서 오류가 발생했습니다.", "PAYMENTS_CONFIRM_400_25"),
    EXCEED_MAX_AMOUNT(400, "거래금액 한도를 초과했습니다.", "PAYMENTS_CONFIRM_400_26"),
    INVALID_ACCOUNT_INFO_RE_REGISTER(400, "유효하지 않은 계좌입니다. 계좌 재등록 후 시도해주세요.", "PAYMENTS_CONFIRM_400_27"),
    NOT_AVAILABLE_PAYMENT(400, "결제가 불가능한 시간대입니다.", "PAYMENTS_CONFIRM_400_28"),
    UNAPPROVED_ORDER_ID(400, "아직 승인되지 않은 주문번호입니다.", "PAYMENTS_CONFIRM_400_29"),
    EXCEED_MAX_MONTHLY_PAYMENT_AMOUNT(400, "당월 결제 가능금액인 1,000,000원을 초과 하셨습니다.", "PAYMENTS_CONFIRM_400_30");

    private final Integer status;
    private final String message;
    private final String code;

    @Override
    public ErrorReason getErrorReason() {
        return new ErrorReason(status, message, code);
    }
}
