package com.mavis.domain.domains.order.domain;

import com.mavis.domain.domains.common.jpa.BaseEntity;
import com.mavis.domain.domains.delivery.domain.Delivery;
import com.mavis.domain.domains.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

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

    @Column(columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderStatus orderStatus = OrderStatus.READY;

    @Embedded
    private OrderAddress orderAddress;

    @OneToMany(mappedBy = "order")
    private List<OrderItem> orderItems;

    @OneToOne(mappedBy = "order") // Delivery 쪽에 FK가 있을 때
    private Delivery delivery;

    @Builder.Default
    private boolean isDeleted = false;

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
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
    }

    public String getDisplayStatus() {
        if (orderStatus == OrderStatus.CANCELED) {
            return orderStatus.getTitle();
        }
        return delivery != null ? delivery.getDeliveryStatus().getTitle() : orderStatus.getTitle();
    }
}
