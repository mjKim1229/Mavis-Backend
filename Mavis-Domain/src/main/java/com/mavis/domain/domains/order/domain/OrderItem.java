package com.mavis.domain.domains.order.domain;

import com.mavis.domain.domains.common.jpa.BaseEntity;
import com.mavis.domain.domains.product.domain.Product;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

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

    private String productName;

    @Comment("주문 시점 상품 단가 (product.price 스냅샷)")
    private int unitPrice;

    @Comment("unit_price × quantity. 배송비 미포함")
    private int totalPrice;

    private String color;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Builder.Default
    private boolean isDeleted = false;


    public static OrderItem of(OrderOption option, int unitPrice, int totalPrice, Order order, Product product) {
        return OrderItem.builder()
                .color(option.color())
                .quantity(option.quantity())
                .order(order)
                .product(product)
                .productName(product.getName())
                .unitPrice(unitPrice)
                .totalPrice(totalPrice)
                .build();
    }
}
