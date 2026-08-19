package com.mavis.admin.domains.refund.facade;

import com.mavis.admin.domains.refund.dto.RefundValidateInfo;
import com.mavis.admin.domains.refund.service.AdminRefundService;
import com.mavis.common.properties.TossPaymentsProperties;
import com.mavis.infrastructure.outer.api.tosspayments.client.PaymentsCancelClient;
import com.mavis.domain.domains.order.domain.IdempotencyStatus;
import com.mavis.domain.domains.order.domain.PaymentApiType;
import com.mavis.domain.domains.order.domain.PaymentIdempotency;
import com.mavis.domain.domains.order.domain.RefundReceiveAccount;
import com.mavis.domain.domains.order.exception.CancelEntryNotFoundException;
import com.mavis.domain.domains.order.implement.PaymentIdempotencyManager;
import com.mavis.infrastructure.outer.api.tosspayments.dto.CancelPaymentsRequest;
import com.mavis.infrastructure.outer.api.tosspayments.dto.PaymentsCancels;
import com.mavis.infrastructure.outer.api.tosspayments.dto.PaymentsResponse;
import com.mavis.infrastructure.outer.api.tosspayments.dto.RefundReceiveAccountRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class AdminRefundFacade {

    private final TossPaymentsProperties tossPaymentsProperties;
    private final PaymentsCancelClient paymentsCancelClient;
    private final AdminRefundService adminRefundService;
    private final PaymentIdempotencyManager paymentIdempotencyManager;

    public void approveRefund(String idempotencyKey, String testCode, Long refundId) {
        PaymentIdempotency idempotency = paymentIdempotencyManager.startProcessing(idempotencyKey, PaymentApiType.REFUND);
        if (idempotency.getStatus() == IdempotencyStatus.SUCCESS) {
            return;
        }

        // TX1 (read-only): 상태 검증 + paymentKey 조회
        RefundValidateInfo info = adminRefundService.validateForApproval(refundId);

        RefundReceiveAccount account = info.refundReceiveAccount();
        RefundReceiveAccountRequest refundReceiveAccountRequest = account != null
                ? new RefundReceiveAccountRequest(account.getRefundReceiveBankCode(), account.getRefundReceiveAccountNumber(), account.getRefundReceiveHolderName())
                : null;

        CancelPaymentsRequest cancelRequest = new CancelPaymentsRequest(info.refundReason(), info.refundAmount(), refundReceiveAccountRequest);
        log.info("[TOSS][REFUND] 요청 - {}", cancelRequest);
        PaymentsResponse response;
        try {
            response = paymentsCancelClient.cancelPayments(
                    tossPaymentsProperties.getAuthorizationHeader(),
                    idempotencyKey,
                    testCode,
                    info.paymentKey(),
                    cancelRequest
            );
            log.info("[TOSS][REFUND] 완료 - {}", response);
        } catch (Exception e) {
            log.error("[TOSS][REFUND] 실패 - {}", cancelRequest, e);
            paymentIdempotencyManager.markFailure(idempotency.getId(), e.getMessage());
            throw e;
        }

        PaymentsCancels cancelEntry = response.currentCancelEntry();
        if (cancelEntry == null) throw CancelEntryNotFoundException.EXCEPTION;

        // TX2: approve + Payment(CANCEL) INSERT + complete
        try {
            adminRefundService.approveAndComplete(info.refundId(), response, cancelEntry);
            paymentIdempotencyManager.markSuccess(idempotency.getId());
        } catch (Exception e) {
            log.error("[TOSS][REFUND] 후처리 실패 - {}", response, e);
            paymentIdempotencyManager.markFailure(idempotency.getId(), e.getMessage());
            throw e;
        }
    }
}
