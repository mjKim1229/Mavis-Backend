package com.mavis.infrastructure.outer.api.tosspayments.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record PaymentsResponse(
        String version,
        String paymentKey,
        String type,
        String orderId,
        String orderName,
        String mId,
        String currency,
        TossPaymentMethod method,
        int totalAmount,
        Long balanceAmount,
        PaymentsStatus status,
        OffsetDateTime requestedAt,
        OffsetDateTime approvedAt,
        Boolean useEscrow,
        String lastTransactionKey,
        Long suppliedAmount,
        Long vat,
        Boolean cultureExpense,
        Long taxFreeAmount,
        Long taxExemptionAmount,
        List<PaymentsCancels> cancels,
        Boolean isPartialCancelable,
        PaymentsReceipt receipt,
        PaymentsCheckout checkout,
        PaymentsEasyPay easyPay,
        PaymentsCard card,
        PaymentsVirtualAccount virtualAccount,
        String secret,
        String country,
        PaymentsFailure failure,
        PaymentsCashReceipt cashReceipt,
        PaymentsCardPromotion discount
) {
    public String easyPayProvider() {
        return easyPay != null ? easyPay.provider().name() : null;
    }

    public java.time.LocalDateTime approvedAtLocal() {
        return approvedAt != null ? approvedAt.toLocalDateTime() : null;
    }

    public String cardNumber() {
        return card != null ? card.number() : null;
    }

    public String cardIssuerCode() {
        return card != null ? card.issuerCode().name() : null;
    }

    public String receiptUrl() {
        return receipt != null ? receipt.url() : null;
    }

    public String virtualAccountNumber() {
        return virtualAccount != null ? virtualAccount.accountNumber() : null;
    }

    public String virtualAccountBankCode() {
        return virtualAccount != null ? virtualAccount.bankCode() : null;
    }

    public java.time.LocalDateTime virtualAccountDueDateLocal() {
        return virtualAccount != null && virtualAccount.dueDate() != null
                ? virtualAccount.dueDate().toLocalDateTime() : null;
    }

    public String virtualAccountDepositorName() {
        return virtualAccount != null ? virtualAccount.depositorName() : null;
    }

    public String refundReceiveBankCode() {
        return virtualAccount != null ? virtualAccount.refundReceiveAccount().bankCode() : null;
    }

    public String refundReceiveAccountNumber() {
        return virtualAccount != null ? virtualAccount.refundReceiveAccount().accountNumber() : null;
    }

    public String refundReceiveHolderName() {
        return virtualAccount != null ? virtualAccount.refundReceiveAccount().holderName() : null;
    }

    public PaymentsCancels currentCancelEntry() {
        if (cancels == null || cancels.isEmpty() || lastTransactionKey == null) return null;
        return cancels.stream()
                .filter(c -> lastTransactionKey.equals(c.transactionKey()))
                .findFirst()
                .orElse(null);
    }
}
