package com.mavis.domain.domains.claim.domain;

import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.refund.exception.CannotRefundException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClaimTest {

    private final Order order = Order.builder().build();

    private OrderItem orderItem(int totalPrice) {
        return OrderItem.builder().order(order).totalPrice(totalPrice).build();
    }

    @Test
    void 주문취소_클레임은_완료_상태로_주문의_상품을_모두_담는다() {
        OrderItem item1 = orderItem(10000);
        OrderItem item2 = orderItem(20000);

        Claim claim = Claim.cancel(order, List.of(item1, item2), "단순 변심");

        assertThat(claim.getClaimType()).isEqualTo(ClaimType.CANCEL);
        assertThat(claim.getClaimStatus()).isEqualTo(ClaimStatus.COMPLETED);
        assertThat(claim.getFaultParty()).isEqualTo(FaultParty.BUYER);
        assertThat(claim.getCompletedAt()).isNotNull();
        assertThat(claim.getItems()).extracting(ClaimItem::getOrderItem).containsExactly(item1, item2);
        assertThat(claim.getItemsTotalPrice()).isEqualTo(30000);
    }

    @Test
    void 반품_신청은_귀책과_처리시각_없이_요청_상태로_시작한다() {
        OrderItem item = orderItem(10000);

        Claim claim = Claim.requestReturn(item, "불량");

        assertThat(claim.getClaimType()).isEqualTo(ClaimType.RETURN);
        assertThat(claim.getClaimStatus()).isEqualTo(ClaimStatus.REQUESTED);
        assertThat(claim.getOrder()).isSameAs(order);
        assertThat(claim.getFaultParty()).isNull();
        assertThat(claim.getCompletedAt()).isNull();
    }

    @Test
    void 반품_승인시_귀책과_처리시각이_기록된다() {
        Claim claim = Claim.requestReturn(orderItem(10000), "불량");

        claim.complete(FaultParty.SELLER);

        assertThat(claim.getClaimStatus()).isEqualTo(ClaimStatus.COMPLETED);
        assertThat(claim.getFaultParty()).isEqualTo(FaultParty.SELLER);
        assertThat(claim.getCompletedAt()).isNotNull();
    }

    @Test
    void 반품_거절시_귀책은_비어있고_처리시각이_기록된다() {
        Claim claim = Claim.requestReturn(orderItem(10000), "변심");

        claim.reject();

        assertThat(claim.getClaimStatus()).isEqualTo(ClaimStatus.REJECTED);
        assertThat(claim.getFaultParty()).isNull();
        assertThat(claim.getCompletedAt()).isNotNull();
    }

    @Test
    void 이미_처리된_반품은_다시_승인하거나_거절할_수_없다() {
        Claim rejected = Claim.requestReturn(orderItem(10000), "변심");
        rejected.reject();

        assertThatThrownBy(() -> rejected.complete(FaultParty.BUYER)).isInstanceOf(CannotRefundException.class);
        assertThatThrownBy(rejected::reject).isInstanceOf(CannotRefundException.class);
    }

    @Test
    void 주문취소_클레임은_승인하거나_거절할_수_없다() {
        Claim cancel = Claim.cancel(order, List.of(orderItem(10000)), "변심");

        assertThatThrownBy(() -> cancel.complete(FaultParty.BUYER)).isInstanceOf(CannotRefundException.class);
        assertThatThrownBy(cancel::reject).isInstanceOf(CannotRefundException.class);
    }
}
