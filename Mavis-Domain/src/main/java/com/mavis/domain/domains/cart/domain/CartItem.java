package com.mavis.domain.domains.cart.domain;

import com.mavis.common.enums.Size;
import com.mavis.domain.domains.common.jpa.BaseEntity;
import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem extends BaseEntity {
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

    @Comment("선택한 사이즈. 사이즈 옵션이 없는 상품은 NULL")
    @Column(columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    private Size size;

    @Builder.Default
    private boolean isDeleted = false;

    public void update(int quantity, String color, Size size) {
        this.quantity = quantity;
        this.color = color;
        this.size = size;
    }

    public void increaseQuantity(int additionalQuantity) {
        this.quantity += additionalQuantity;
    }

    public void delete() {
        this.isDeleted = true;
    }
}
