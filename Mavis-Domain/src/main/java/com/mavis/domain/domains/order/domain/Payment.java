package com.mavis.domain.domains.order.domain;

import com.mavis.domain.domains.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_key", length = 200)
    private String paymentKey;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private PaymentMethod method;

    private Long totalAmount;

    private Long balanceAmount;

    private ZonedDateTime requestedAt;

    private ZonedDateTime approvedAt;

    @Column(length = 200)
    private String lastTransactionKey;

    private Boolean partialCancelable;

    private String provider;

    private String cardNumber;

    private String receiptUrl;
}
