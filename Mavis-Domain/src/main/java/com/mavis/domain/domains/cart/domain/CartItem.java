package com.mavis.domain.domains.cart.domain;

import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "orders")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private int quantity;

    private String color;

    @Builder.Default
    private boolean isDeleted = false;

    public void update(int quantity, String color) {
        this.quantity = quantity;
        this.color = color;
    }

    public void delete() {
        this.isDeleted = true;
    }
}
