package com.mavis.domain.domains.refund.domain;

import com.mavis.domain.domains.common.jpa.BaseEntity;
import com.mavis.domain.domains.order.domain.OrderItem;
import jakarta.persistence.*;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Refund extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;

    @Column(columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RefundStatus refundStatus = RefundStatus.REQUESTED;

    private String refundReason;

    private int refundQuantity;

    private int refundAmount;

    private String cancelTransactionKey;

    public void approve() {
        this.refundStatus = RefundStatus.APPROVED;
    }

    public void reject() {
        this.refundStatus = RefundStatus.REJECTED;
    }

    public void complete(String cancelTransactionKey) {
        this.refundStatus = RefundStatus.COMPLETED;
        this.cancelTransactionKey = cancelTransactionKey;
    }
}
