package com.mavis.domain.domains.refund.domain;

import com.mavis.domain.domains.common.jpa.BaseEntity;
import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.order.domain.Payment;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.*;
import org.hibernate.annotations.Comment;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Refund extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;

    @Comment("FK → payment.id (CANCEL 행). RETURN은 어드민 승인 전까지 NULL")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @Column(columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RefundStatus refundStatus = RefundStatus.REQUESTED;

    @Column(columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    private RefundType refundType;

    private String refundReason;

    @Comment("배송비 제외 순수 상품 환불금액 (OrderItem.total_price 기준)")
    private int refundAmount;

    @Comment("환불 완료 시 Toss 취소 거래 키 (연결된 payment.last_transaction_key와 같은 값)")
    private String cancelTransactionKey;

    @Comment("RETURN: 고객이 반품 발송한 택배사 (배송 송장은 delivery 테이블)")
    private String carrier;

    @Comment("RETURN: 고객이 반품 발송한 송장번호")
    private String trackingNumber;

    private LocalDateTime processedAt;

    @Builder.Default
    @OneToMany(mappedBy = "refund", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<RefundImage> images = new ArrayList<>();

    public void linkPayment(Payment payment) {
        this.payment = payment;
    }

    public void reject() {
        this.refundStatus = RefundStatus.REJECTED;
        this.processedAt = LocalDateTime.now();
    }

    public void complete(String cancelTransactionKey) {
        this.refundStatus = RefundStatus.COMPLETED;
        this.cancelTransactionKey = cancelTransactionKey;
        this.processedAt = LocalDateTime.now();
    }
}
