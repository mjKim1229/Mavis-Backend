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

    @Comment("외부 결제 호출 성공 후 후처리가 실패한 경우(NEEDS_RECONCILE) 복구 조회용 Toss paymentKey")
    @Column(length = 200)
    private String paymentKey;

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

    /**
     * 외부 결제 호출은 성공했으나 후처리가 실패한 상태로 기록한다.
     * paymentKey는 이후 Toss 조회로 실제 상태를 확인하기 위해 남긴다.
     */
    public void needsReconcile(String reason, String paymentKey) {
        this.status = IdempotencyStatus.NEEDS_RECONCILE;
        this.failureReason = reason;
        this.paymentKey = paymentKey;
    }
}
