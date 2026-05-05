package com.mavis.domain.domains.order.domain;

import com.mavis.domain.domains.common.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

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

    private int totalAmount;

    private Long balanceAmount;

    private LocalDateTime requestedAt;

    private LocalDateTime approvedAt;

    @Column(length = 200)
    private String lastTransactionKey;

    private Boolean partialCancelable;

    private Integer cancelAmount;

    private String cancelReason;

    private LocalDateTime canceledAt;

    @Embedded
    private CardInfo cardInfo;

    private String virtualAccountSecret;

    @Embedded
    private VirtualAccountInfo virtualAccountInfo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;
}
