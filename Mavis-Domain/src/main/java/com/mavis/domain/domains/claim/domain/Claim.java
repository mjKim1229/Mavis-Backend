package com.mavis.domain.domains.claim.domain;

import com.mavis.domain.domains.common.jpa.BaseEntity;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.refund.exception.CannotRefundException;
import com.mavis.domain.domains.refund.exception.InvalidRefundAmountException;
import com.mavis.domain.domains.refund.exception.InvalidRefundDecisionException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Claim extends BaseEntity {

    public static final int RETURN_SHIPPING_FEE = 4000;
    private static final int MAX_REASON_LENGTH = 200;

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

    @Comment("CHANGE_OF_MIND: 단순 변심 / SELLER_FAULT: 판매자 귀책. 고객이 반품 신청 시 선택, 취소 클레임은 NULL")
    @Column(columnDefinition = "varchar(255)")
    @Enumerated(EnumType.STRING)
    private ClaimReasonType reasonType;

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

    private Claim(Order order, ClaimType claimType, ClaimStatus claimStatus, ClaimReasonType reasonType,
                  String reason, FaultParty faultParty, LocalDateTime completedAt) {
        this.order = order;
        this.claimType = claimType;
        this.claimStatus = claimStatus;
        this.reasonType = reasonType;
        this.reason = reason;
        this.faultParty = faultParty;
        this.completedAt = completedAt;
    }

    public static Claim cancel(Order order, List<OrderItem> orderItems, String reason) {
        LocalDateTime now = LocalDateTime.now();
        Claim claim = new Claim(order, ClaimType.CANCEL, ClaimStatus.COMPLETED, null, reason, FaultParty.BUYER, now);
        orderItems.forEach(orderItem -> claim.addItem(orderItem, null));
        return claim;
    }

    public static Claim requestReturn(Order order, ClaimReasonType reasonType, List<ReturnItemReason> returnItems) {
        String reason = joinReasons(reasonType, returnItems);
        Claim claim = new Claim(order, ClaimType.RETURN, ClaimStatus.REQUESTED, reasonType, reason, null, null);
        returnItems.forEach(returnItem -> claim.addItem(returnItem.orderItem(), returnItem.reason()));
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

    public int deductedRefundAmount(boolean orderShippingSettled) {
        int orderShippingFee = orderShippingFee(orderShippingSettled);
        return getItemsTotalPrice() - orderShippingFee - RETURN_SHIPPING_FEE;
    }

    public int fullRefundAmount(boolean orderShippingSettled) {
        int orderShippingFee = orderShippingFee(orderShippingSettled);
        return getItemsTotalPrice() + orderShippingFee;
    }

    public ReturnRefundPlan planReturnRefund(ReturnDecision decision, Integer partialAmount,
                                             boolean orderShippingSettled, int maxRefundable) {
        validateReturnRequested();
        ReturnDecision resolved = resolveDecision(decision);
        int orderShippingFee = orderShippingFee(orderShippingSettled);
        ReturnRefundPlan plan = switch (resolved) {
            case NOT_SELLER_FAULT -> new ReturnRefundPlan(deductedRefundAmount(orderShippingSettled), 0, orderShippingFee, RETURN_SHIPPING_FEE, FaultParty.BUYER);
            case FULL_REFUND -> new ReturnRefundPlan(fullRefundAmount(orderShippingSettled), orderShippingFee, 0, 0, FaultParty.SELLER);
            case PARTIAL_REFUND -> new ReturnRefundPlan(requirePartialAmount(partialAmount), 0, 0, 0, FaultParty.SELLER);
        };
        if (plan.totalAmount() < 1 || plan.totalAmount() > maxRefundable) {
            throw InvalidRefundAmountException.EXCEPTION;
        }
        return plan;
    }

    public int orderShippingFee(boolean orderShippingSettled) {
        if (orderShippingSettled) {
            return 0;
        }
        return order.getDeliveryFee();
    }

    private ReturnDecision resolveDecision(ReturnDecision decision) {
        if (reasonType == ClaimReasonType.SELLER_FAULT) {
            if (decision == null) {
                throw InvalidRefundDecisionException.EXCEPTION;
            }
            return decision;
        }
        if (decision != null && decision != ReturnDecision.NOT_SELLER_FAULT) {
            throw InvalidRefundDecisionException.EXCEPTION;
        }
        return ReturnDecision.NOT_SELLER_FAULT;
    }

    private int requirePartialAmount(Integer partialAmount) {
        if (partialAmount == null) {
            throw InvalidRefundAmountException.EXCEPTION;
        }
        return partialAmount;
    }

    private static String joinReasons(ClaimReasonType reasonType, List<ReturnItemReason> returnItems) {
        String joined = returnItems.stream()
                .map(ReturnItemReason::reason)
                .filter(reason -> reason != null && !reason.isBlank())
                .collect(Collectors.joining(" / "));
        if (joined.isEmpty()) {
            return reasonType.getTitle();
        }
        if (joined.length() > MAX_REASON_LENGTH) {
            return joined.substring(0, MAX_REASON_LENGTH);
        }
        return joined;
    }

    private void addItem(OrderItem orderItem, String reason) {
        ClaimItem item = ClaimItem.of(this, orderItem, reason);
        items.add(item);
    }

    private void validateReturnRequested() {
        if (claimType != ClaimType.RETURN || claimStatus != ClaimStatus.REQUESTED) {
            throw CannotRefundException.EXCEPTION;
        }
    }
}
