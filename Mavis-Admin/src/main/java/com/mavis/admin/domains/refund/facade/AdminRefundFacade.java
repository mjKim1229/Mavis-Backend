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

    public void approveRefund(String idempotencyKey, String testCode, Long claimId) {
        PaymentIdempotency idempotency = paymentIdempotencyManager.startProcessing(idempotencyKey, PaymentApiType.REFUND);
        if (idempotency.getStatus() == IdempotencyStatus.SUCCESS) {
            return;
        }

        RefundValidateInfo info;
        PaymentsResponse response;
        try {
            // TX1 (read-only): 상태 검증 + paymentKey 조회, 이어서 토스 취소 호출
            info = adminRefundService.validateForApproval(claimId);

            RefundReceiveAccount account = info.refundReceiveAccount();
            RefundReceiveAccountRequest refundReceiveAccountRequest = account != null
                    ? new RefundReceiveAccountRequest(account.getRefundReceiveBankCode(), account.getRefundReceiveAccountNumber(), account.getRefundReceiveHolderName())
                    : null;

            CancelPaymentsRequest cancelRequest = new CancelPaymentsRequest(info.refundReason(), info.refundAmount(), refundReceiveAccountRequest);
            log.info("[TOSS][REFUND] 요청 - {}", cancelRequest);
            response = paymentsCancelClient.cancelPayments(
                    tossPaymentsProperties.getAuthorizationHeader(),
                    idempotencyKey,
                    testCode,
                    info.paymentKey(),
                    cancelRequest
            );
            log.info("[TOSS][REFUND] 완료 - {}", response);
        } catch (Exception e) {
            // 여기까지는 환불이 일어나지 않았거나 호출 자체가 실패한 구간 — 재시도 가능
            log.error("[TOSS][REFUND] 실패 - claimId={}", claimId, e);
            paymentIdempotencyManager.markFailure(idempotency.getId(), e.getMessage());
            throw e;
        }

        try {
            PaymentsCancels cancelEntry = response.currentCancelEntry();
            if (cancelEntry == null) throw CancelEntryNotFoundException.EXCEPTION;

            // TX2: approve + Payment(CANCEL) INSERT + complete
            adminRefundService.approveAndComplete(info.claimId(), response, cancelEntry);
            paymentIdempotencyManager.markSuccess(idempotency.getId());
        } catch (Exception e) {
            // 환불은 이미 완료됐고 DB만 반영되지 않은 상태 — 재시도가 아니라 사람의 확인이 필요하다
            log.error("[TOSS][REFUND] 후처리 실패 — 환불은 완료됨, 정합성 확인 필요 - claimId={}", claimId, e);
            paymentIdempotencyManager.markNeedsReconcile(idempotency.getId(), e.getMessage(), info.paymentKey());
            throw e;
        }
    }
}
