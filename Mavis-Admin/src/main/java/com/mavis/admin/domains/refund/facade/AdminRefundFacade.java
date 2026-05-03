package com.mavis.admin.domains.refund.facade;

import com.mavis.admin.domains.refund.dto.RefundValidateInfo;
import com.mavis.admin.domains.refund.service.AdminRefundService;
import com.mavis.common.properties.TossPaymentsProperties;
import com.mavis.infrastructure.outer.api.tosspayments.client.PaymentsCancelClient;
import com.mavis.infrastructure.outer.api.tosspayments.dto.CancelPaymentsRequest;
import com.mavis.infrastructure.outer.api.tosspayments.dto.PaymentsResponse;
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

    public void approveRefund(String idempotencyKey, String testCode, Long refundId) {
        // TX1 (read-only): 상태 검증 + paymentKey 조회
        RefundValidateInfo info = adminRefundService.validateForApproval(refundId);

        // 외부 API
        PaymentsResponse response = paymentsCancelClient.cancelPayments(
                tossPaymentsProperties.getAuthorizationHeader(),
                idempotencyKey,
                testCode,
                info.paymentKey(),
                new CancelPaymentsRequest(info.refundReason(), info.refundAmount())
        );

        log.info("Toss 환불 취소 응답: {}", response);

        String cancelTransactionKey = response.cancels() != null && !response.cancels().isEmpty()
                ? response.cancels().get(0).transactionKey()
                : null;

        // TX2: approve + Payment(CANCEL) INSERT + complete
        adminRefundService.approveAndComplete(info.refundId(), cancelTransactionKey);
    }
}
