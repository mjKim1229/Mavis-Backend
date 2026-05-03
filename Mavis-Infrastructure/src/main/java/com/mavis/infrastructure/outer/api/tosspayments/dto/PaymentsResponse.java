package com.mavis.infrastructure.outer.api.tosspayments.dto;

import java.time.ZonedDateTime;
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
        ZonedDateTime requestedAt,
        ZonedDateTime approvedAt,
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

    public String receiptUrl() {
        return receipt != null ? receipt.url() : null;
    }
}
