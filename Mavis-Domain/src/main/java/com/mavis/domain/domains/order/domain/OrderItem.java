package com.mavis.domain.domains.order.domain;

import com.mavis.domain.domains.common.jpa.BaseEntity;
import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.refund.domain.Refund;
import com.mavis.domain.domains.refund.domain.RefundStatus;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private int quantity;

    private int price;

    private String color;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @OneToOne(mappedBy = "orderItem")
    private Refund refund;

    @Builder.Default
    private boolean isDeleted = false;


    public int getTotalPrice() {
        return price * quantity;
    }

    public RefundStatus getRefundStatus() {
        return refund != null ? refund.getRefundStatus() : null;
    }

    public static OrderItem of(OrderOption option, int price, Order order, Product product) {
        return OrderItem.builder()
                .color(option.color())
                .quantity(option.quantity())
                .order(order)
                .product(product)
                .price(price)
                .build();
    }
}
