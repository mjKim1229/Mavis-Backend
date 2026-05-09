package com.mavis.domain.domains.refund.domain;

import com.mavis.domain.domains.common.jpa.BaseEntity;
import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.order.domain.Payment;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;

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

    private int refundAmount;

    private String cancelTransactionKey;

    private String carrier;

    private String trackingNumber;

    @Builder.Default
    @OneToMany(mappedBy = "refund", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<RefundImage> images = new ArrayList<>();

    public void linkPayment(Payment payment) {
        this.payment = payment;
    }

    public void reject() {
        this.refundStatus = RefundStatus.REJECTED;
    }

    public void complete(String cancelTransactionKey) {
        this.refundStatus = RefundStatus.COMPLETED;
        this.cancelTransactionKey = cancelTransactionKey;
    }
}
