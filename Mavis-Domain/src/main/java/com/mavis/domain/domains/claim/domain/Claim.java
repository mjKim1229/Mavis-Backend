package com.mavis.domain.domains.claim.domain;

import com.mavis.domain.domains.common.jpa.BaseEntity;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.refund.exception.CannotRefundException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Claim extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Comment("FK → orders.id")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Comment("CANCEL: 배송 전 주문 취소 / RETURN: 배송 후 반품")
    @Column(nullable = false, columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    private ClaimType claimType;

    @Comment("REQUESTED: 신청됨 / COMPLETED: 처리 완료 / REJECTED: 거절")
    @Column(nullable = false, columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    private ClaimStatus claimStatus;

    @Comment("고객이 입력한 사유")
    private String reason;

    @Comment("BUYER: 구매자 귀책 / SELLER: 판매자 귀책. 배송비 환불 여부 기준, 검수 전 NULL")
    @Column(columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    private FaultParty faultParty;

    @Comment("완료 또는 거절 처리 시각. 처리 전 NULL")
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "claim", cascade = CascadeType.ALL)
    private List<ClaimItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "claim", cascade = CascadeType.ALL)
    private List<ClaimImage> images = new ArrayList<>();

    private Claim(Order order, ClaimType claimType, ClaimStatus claimStatus, String reason,
                  FaultParty faultParty, LocalDateTime completedAt) {
        this.order = order;
        this.claimType = claimType;
        this.claimStatus = claimStatus;
        this.reason = reason;
        this.faultParty = faultParty;
        this.completedAt = completedAt;
    }

    public static Claim cancel(Order order, List<OrderItem> orderItems, String reason) {
        LocalDateTime now = LocalDateTime.now();
        Claim claim = new Claim(order, ClaimType.CANCEL, ClaimStatus.COMPLETED, reason, FaultParty.BUYER, now);
        orderItems.forEach(claim::addItem);
        return claim;
    }

    public static Claim requestReturn(OrderItem orderItem, String reason) {
        Order order = orderItem.getOrder();
        Claim claim = new Claim(order, ClaimType.RETURN, ClaimStatus.REQUESTED, reason, null, null);
        claim.addItem(orderItem);
        return claim;
    }

    public void addImage(String imageUrl) {
        ClaimImage image = ClaimImage.of(this, imageUrl);
        images.add(image);
    }

    public void complete(FaultParty faultParty) {
        validateReturnRequested();
        this.claimStatus = ClaimStatus.COMPLETED;
        this.faultParty = faultParty;
        this.completedAt = LocalDateTime.now();
    }

    public void reject() {
        validateReturnRequested();
        this.claimStatus = ClaimStatus.REJECTED;
        this.completedAt = LocalDateTime.now();
    }

    public int getItemsTotalPrice() {
        return items.stream()
                .mapToInt(item -> item.getOrderItem().getTotalPrice())
                .sum();
    }

    private void addItem(OrderItem orderItem) {
        ClaimItem item = ClaimItem.of(this, orderItem);
        items.add(item);
    }

    private void validateReturnRequested() {
        if (claimType != ClaimType.RETURN || claimStatus != ClaimStatus.REQUESTED) {
            throw CannotRefundException.EXCEPTION;
        }
    }
}
