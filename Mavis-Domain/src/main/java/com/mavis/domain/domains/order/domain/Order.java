package com.mavis.domain.domains.order.domain;

import com.mavis.domain.domains.common.jpa.BaseEntity;
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

    private String orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private int totalPrice;

    @Column(columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrderStatus orderStatus = OrderStatus.ORDERED;

    @Embedded
    private OrderAddress orderAddress;

    @OneToMany(mappedBy = "order")
    private List<OrderItem> orderItems;

    @Builder.Default
    private boolean isDeleted = false;

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void confirm() {
        this.orderStatus = OrderStatus.CONFIRMED;
    }

    @Builder.Default
    private boolean isPayConfirmed = false;

    public void setPayConfirmed() {
        this.isPayConfirmed = true;
    }
}
