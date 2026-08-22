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
@Table(
    name = "payment_idempotency",
    uniqueConstraints = @UniqueConstraint(columnNames = {"idempotency_key", "api_type"})
)
public class PaymentIdempotency extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key", nullable = false, length = 300)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "api_type", nullable = false, columnDefinition = "varchar(20)")
    private PaymentApiType apiType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20)")
    private IdempotencyStatus status;

    @Column(columnDefinition = "TEXT")
    private String failureReason;

    @Column(nullable = false)
    private LocalDateTime expiredAt;

    public static PaymentIdempotency ofProcessing(String idempotencyKey, PaymentApiType apiType, LocalDateTime expiredAt) {
        return PaymentIdempotency.builder()
                .idempotencyKey(idempotencyKey)
                .apiType(apiType)
                .status(IdempotencyStatus.PROCESSING)
                .expiredAt(expiredAt)
                .build();
    }

    public void success() {
        this.status = IdempotencyStatus.SUCCESS;
    }

    public void fail(String reason) {
        this.status = IdempotencyStatus.FAILURE;
        this.failureReason = reason;
    }
}
