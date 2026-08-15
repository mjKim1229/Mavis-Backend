package com.mavis.domain.domains.order.domain;

import com.mavis.domain.domains.common.jpa.BaseEntity;
import com.mavis.domain.domains.delivery.domain.DeliveryStatus;
import com.mavis.domain.domains.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

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

    @Column(unique = true)
    private String orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private int totalPrice;

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
