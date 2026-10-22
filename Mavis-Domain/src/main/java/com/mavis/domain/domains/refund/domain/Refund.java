package com.mavis.domain.domains.refund.domain;

import com.mavis.domain.domains.claim.domain.Claim;
import com.mavis.domain.domains.claim.domain.ReturnRefundPlan;
import com.mavis.domain.domains.common.jpa.BaseEntity;
import com.mavis.domain.domains.order.domain.Payment;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Refund extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("FK → claim.id")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id", nullable = false)
    private Claim claim;

    @Comment("FK → payment.id (CANCEL 원장 행)")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Comment("이번에 돌려준 총액 = 토스 취소 금액 (product_amount + shipping_fee_refund)")
    private int totalAmount;

    @Comment("총액 중 상품 금액")
    private int productAmount;

    @Comment("총액 중 배송비. 0이면 배송비 환불 안 함")
    private int shippingFeeRefund;

    @Comment("총액 계산 시 차감한 주문 배송비 (반품 환불에서 단순 변심/귀책 아님일 때만 값 있음). 당시 금액 스냅샷")
    private int orderShippingFeeDeducted;

    @Comment("총액 계산 시 차감한 반품 배송비 (반품 환불에서 단순 변심/귀책 아님일 때만 값 있음). 당시 금액 스냅샷")
    private int returnShippingFeeDeducted;

    @Comment("Toss 취소 거래 키 (payment.last_transaction_key와 같은 값)")
    @Column(nullable = false)
    private String cancelTransactionKey;

    public static Refund forCancel(Claim claim, Payment cancelPayment, int shippingFeeRefund) {
        int totalAmount = cancelPayment.getCancelAmount();
        int productAmount = totalAmount - shippingFeeRefund;
        return new Refund(null, claim, cancelPayment, totalAmount, productAmount, shippingFeeRefund, 0, 0,
                cancelPayment.getLastTransactionKey());
    }

    public static Refund forReturn(Claim claim, Payment cancelPayment, ReturnRefundPlan plan) {
        int totalAmount = cancelPayment.getCancelAmount();
        int productAmount = totalAmount - plan.shippingFeeRefund();
        return new Refund(null, claim, cancelPayment, totalAmount, productAmount, plan.shippingFeeRefund(),
                plan.orderShippingFeeDeducted(), plan.returnShippingFeeDeducted(), cancelPayment.getLastTransactionKey());
    }
}
