package com.mavis.domain.domains.order.domain;

import com.mavis.domain.domains.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String paymentKey;

    @Comment("Toss PG orderId. orders.order_id와 같은 값")
    private String tossOrderId;

    private String orderName;

    private String provider;

    private String receiptUrl;

    @Column(columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;

    @Column(columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PaymentMethod method = PaymentMethod.DEFAULT;

    @Comment("payment_type별 의미 다름 — CONFIRM: 결제 승인 총액 / CANCEL: 이번 취소 금액 (전체취소=orders.total_price 배송비 포함, 반품=refund.refund_amount 배송비 제외) / DEPOSIT: 0. 합산 시 payment_type 필터 필수")
    private int totalAmount;

    @Comment("해당 이벤트 직후 Toss 잔여 취소가능 금액. DEPOSIT 행은 NULL")
    private Long balanceAmount;

    private LocalDateTime requestedAt;

    private LocalDateTime approvedAt;

    @Comment("Toss 거래 키 — CONFIRM: 승인 / CANCEL: 취소 거래 / DEPOSIT: 입금 웹훅")
    @Column(length = 200)
    private String lastTransactionKey;

    private Boolean partialCancelable;

    private LocalDateTime canceledAt;

    @Comment("CANCEL 행만 값 있음: 이번 취소 금액 (= total_amount. 전체취소=배송비 포함, 반품=배송비 제외). 그 외 NULL")
    private Integer cancelAmount;

    private String cancelReason;

    @Embedded
    private CardInfo cardInfo;

    @Comment("Toss 가상계좌 입금 웹훅 검증용 secret (CONFIRM 행)")
    private String virtualAccountSecret;

    @Embedded
    private VirtualAccountInfo virtualAccountInfo;

    @Embedded
    private RefundReceiveAccount refundReceiveAccount;

    @Comment("FK → orders.id (내부 PK). Toss orderId는 toss_order_id")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;
}
