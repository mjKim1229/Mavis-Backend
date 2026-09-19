package com.mavis.domain.domains.order.domain;

import com.mavis.domain.domains.common.jpa.BaseEntity;
import com.mavis.domain.domains.delivery.domain.DeliveryStatus;
import com.mavis.domain.domains.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "orders")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("Toss PG orderId (GARAM 접두어 포함). 내부 PK는 id — payment.order_id는 이 컬럼이 아니라 orders.id 참조")
    @Column(unique = true)
    private String orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Comment("상품금액 합계 + 배송비 포함 총 결제금액")
    private int totalPrice;

    @Comment("배송비 (현재 고정 4000원). total_price에 포함됨")
    private int deliveryFee;

    @Column(columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderStatus orderStatus = OrderStatus.READY;

    @Column(columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Embedded
    private OrderAddress orderAddress;

    @OneToMany(mappedBy = "order")
    private List<OrderItem> orderItems;

    @Builder.Default
    private boolean isDeleted = false;

    private LocalDateTime orderedAt;

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void setDeliveryFee(int deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void paymentRequested() {
        this.orderStatus = OrderStatus.PAYMENT_REQUESTED;
    }

    public void waitingForDeposit() {
        this.orderStatus = OrderStatus.WAITING_FOR_DEPOSIT;
    }

    public void confirmPayment() {
        this.orderStatus = OrderStatus.PAYMENT_CONFIRMED;
    }

    public void cancel() {
        this.orderStatus = OrderStatus.CANCELED;
    }

    public void confirmOrder() {
        this.orderStatus = OrderStatus.ORDERED;
        this.orderedAt = LocalDateTime.now();
    }

    public String resolveDisplayStatus(DeliveryStatus deliveryStatus) {
        if (orderStatus == OrderStatus.CANCELED) return orderStatus.getTitle();
        return deliveryStatus != null ? deliveryStatus.getTitle() : orderStatus.getTitle();
    }

    public String resolveDisplayStatusCode(DeliveryStatus deliveryStatus) {
        if (orderStatus == OrderStatus.CANCELED) return orderStatus.getCode();
        return deliveryStatus != null ? deliveryStatus.getCode() : orderStatus.getCode();
    }
}
