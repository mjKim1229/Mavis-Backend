package com.mavis.domain.domains.claim.domain;

import com.mavis.domain.domains.common.jpa.BaseEntity;
import com.mavis.domain.domains.order.domain.OrderItem;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class ClaimItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("FK → claim.id")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_id", nullable = false)
    private Claim claim;

    @Comment("FK → order_item.id. 상품당 클레임 1건 (중복 신청 차단)")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false, unique = true)
    private OrderItem orderItem;

    static ClaimItem of(Claim claim, OrderItem orderItem) {
        return new ClaimItem(null, claim, orderItem);
    }
}
