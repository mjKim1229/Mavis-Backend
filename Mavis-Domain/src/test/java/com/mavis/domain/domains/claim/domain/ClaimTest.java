package com.mavis.domain.domains.claim.domain;

import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.refund.exception.CannotRefundException;
import com.mavis.domain.domains.refund.exception.InvalidRefundAmountException;
import com.mavis.domain.domains.refund.exception.InvalidRefundDecisionException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClaimTest {

    private final Order order = Order.builder().deliveryFee(4000).build();

    private OrderItem orderItem(int totalPrice) {
        return OrderItem.builder().order(order).totalPrice(totalPrice).build();
    }

    private Claim returnClaim(ClaimReasonType reasonType, int... itemPrices) {
        List<ReturnItemReason> items = Arrays.stream(itemPrices)
                .mapToObj(this::orderItem)
                .map(orderItem -> new ReturnItemReason(orderItem, "사유"))
                .toList();
        return Claim.requestReturn(order, reasonType, items);
    }

    @Test
    void 주문취소_클레임은_완료_상태로_주문의_상품을_모두_담는다() {
        OrderItem item1 = orderItem(10000);
        OrderItem item2 = orderItem(20000);

        Claim claim = Claim.cancel(order, List.of(item1, item2), "단순 변심");

        assertThat(claim.getClaimType()).isEqualTo(ClaimType.CANCEL);
        assertThat(claim.getClaimStatus()).isEqualTo(ClaimStatus.COMPLETED);
        assertThat(claim.getFaultParty()).isEqualTo(FaultParty.BUYER);
        assertThat(claim.getReasonType()).isNull();
        assertThat(claim.getCompletedAt()).isNotNull();
        assertThat(claim.getItems()).extracting(ClaimItem::getOrderItem).containsExactly(item1, item2);
        assertThat(claim.getItemsTotalPrice()).isEqualTo(30000);
    }

    @Test
    void 반품_신청은_귀책과_처리시각_없이_요청_상태로_시작한다() {
        OrderItem item = orderItem(10000);

        Claim claim = Claim.requestReturn(order, ClaimReasonType.SELLER_FAULT, List.of(new ReturnItemReason(item, "불량")));

        assertThat(claim.getClaimType()).isEqualTo(ClaimType.RETURN);
        assertThat(claim.getClaimStatus()).isEqualTo(ClaimStatus.REQUESTED);
        assertThat(claim.getReasonType()).isEqualTo(ClaimReasonType.SELLER_FAULT);
        assertThat(claim.getOrder()).isSameAs(order);
        assertThat(claim.getFaultParty()).isNull();
        assertThat(claim.getCompletedAt()).isNull();
    }

    @Test
    void 반품_신청은_여러_상품을_한_클레임에_담는다() {
        Claim claim = returnClaim(ClaimReasonType.CHANGE_OF_MIND, 10000, 20000);

        assertThat(claim.getItems()).hasSize(2);
        assertThat(claim.getItemsTotalPrice()).isEqualTo(30000);
    }

    @Test
    void 반품_승인시_귀책과_처리시각이_기록된다() {
        Claim claim = returnClaim(ClaimReasonType.SELLER_FAULT, 10000);

        claim.complete(FaultParty.SELLER);

        assertThat(claim.getClaimStatus()).isEqualTo(ClaimStatus.COMPLETED);
        assertThat(claim.getFaultParty()).isEqualTo(FaultParty.SELLER);
        assertThat(claim.getCompletedAt()).isNotNull();
    }

    @Test
    void 반품_거절시_귀책은_비어있고_처리시각이_기록된다() {
        Claim claim = returnClaim(ClaimReasonType.CHANGE_OF_MIND, 10000);

        claim.reject();

        assertThat(claim.getClaimStatus()).isEqualTo(ClaimStatus.REJECTED);
        assertThat(claim.getFaultParty()).isNull();
        assertThat(claim.getCompletedAt()).isNotNull();
    }

    @Test
    void 이미_처리된_반품은_다시_승인하거나_거절할_수_없다() {
        Claim rejected = returnClaim(ClaimReasonType.CHANGE_OF_MIND, 10000);
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

    @Nested
    class 반품_환불액_계산 {

        @Test
        void 변심_첫_반품은_주문_배송비와_반품_배송비를_모두_차감한다() {
            Claim claim = returnClaim(ClaimReasonType.CHANGE_OF_MIND, 30000);

            ReturnRefundPlan plan = claim.planReturnRefund(null, null, false, 34000);

            assertThat(plan.totalAmount()).isEqualTo(26000);
            assertThat(plan.shippingFeeRefund()).isZero();
            assertThat(plan.orderShippingFeeDeducted()).isEqualTo(4000);
            assertThat(plan.returnShippingFeeDeducted()).isEqualTo(4000);
            assertThat(plan.faultParty()).isEqualTo(FaultParty.BUYER);
        }

        @Test
        void 변심_이후_반품은_반품_배송비만_차감한다() {
            Claim claim = returnClaim(ClaimReasonType.CHANGE_OF_MIND, 30000);

            ReturnRefundPlan plan = claim.planReturnRefund(null, null, true, 34000);

            assertThat(plan.totalAmount()).isEqualTo(26000);
            assertThat(plan.orderShippingFeeDeducted()).isZero();
            assertThat(plan.returnShippingFeeDeducted()).isEqualTo(4000);
        }

        @Test
        void 상품이_다른_두_번의_변심_반품은_주문_배송비를_첫_반품에서만_뺀다() {
            Claim first = returnClaim(ClaimReasonType.CHANGE_OF_MIND, 20000);
            Claim second = returnClaim(ClaimReasonType.CHANGE_OF_MIND, 10000);

            ReturnRefundPlan firstPlan = first.planReturnRefund(null, null, false, 34000);
            ReturnRefundPlan secondPlan = second.planReturnRefund(null, null, true, 18000);

            assertThat(firstPlan.totalAmount()).isEqualTo(16000);
            assertThat(firstPlan.orderShippingFeeDeducted()).isEqualTo(4000);
            assertThat(firstPlan.returnShippingFeeDeducted()).isEqualTo(4000);
            assertThat(secondPlan.totalAmount()).isEqualTo(6000);
            assertThat(secondPlan.orderShippingFeeDeducted()).isZero();
            assertThat(secondPlan.returnShippingFeeDeducted()).isEqualTo(4000);
        }

        @Test
        void 여러_상품_묶음_반품도_배송비는_한_번씩만_차감한다() {
            Claim claim = returnClaim(ClaimReasonType.CHANGE_OF_MIND, 10000, 20000);

            ReturnRefundPlan plan = claim.planReturnRefund(null, null, false, 34000);

            assertThat(plan.totalAmount()).isEqualTo(26000);
        }

        @Test
        void 변심은_귀책_아님_결정도_허용하고_동일하게_계산한다() {
            Claim claim = returnClaim(ClaimReasonType.CHANGE_OF_MIND, 30000);

            ReturnRefundPlan plan = claim.planReturnRefund(ReturnDecision.NOT_SELLER_FAULT, null, false, 34000);

            assertThat(plan.totalAmount()).isEqualTo(26000);
        }

        @Test
        void 변심에_전액_또는_일부_환불_결정은_거부한다() {
            Claim claim = returnClaim(ClaimReasonType.CHANGE_OF_MIND, 30000);

            assertThatThrownBy(() -> claim.planReturnRefund(ReturnDecision.FULL_REFUND, null, false, 34000))
                    .isInstanceOf(InvalidRefundDecisionException.class);
            assertThatThrownBy(() -> claim.planReturnRefund(ReturnDecision.PARTIAL_REFUND, 1000, false, 34000))
                    .isInstanceOf(InvalidRefundDecisionException.class);
        }

        @Test
        void 판매자_귀책_전액_환불은_상품금액과_주문_배송비를_돌려준다() {
            Claim claim = returnClaim(ClaimReasonType.SELLER_FAULT, 30000);

            ReturnRefundPlan plan = claim.planReturnRefund(ReturnDecision.FULL_REFUND, null, false, 34000);

            assertThat(plan.totalAmount()).isEqualTo(34000);
            assertThat(plan.shippingFeeRefund()).isEqualTo(4000);
            assertThat(plan.orderShippingFeeDeducted()).isZero();
            assertThat(plan.returnShippingFeeDeducted()).isZero();
            assertThat(plan.faultParty()).isEqualTo(FaultParty.SELLER);
        }

        @Test
        void 판매자_귀책_전액_환불도_주문_배송비가_이미_정산됐으면_상품금액만_돌려준다() {
            Claim claim = returnClaim(ClaimReasonType.SELLER_FAULT, 30000);

            ReturnRefundPlan plan = claim.planReturnRefund(ReturnDecision.FULL_REFUND, null, true, 34000);

            assertThat(plan.totalAmount()).isEqualTo(30000);
            assertThat(plan.shippingFeeRefund()).isZero();
        }

        @Test
        void 판매자_귀책_일부_환불은_어드민이_입력한_금액을_돌려준다() {
            Claim claim = returnClaim(ClaimReasonType.SELLER_FAULT, 30000);

            ReturnRefundPlan plan = claim.planReturnRefund(ReturnDecision.PARTIAL_REFUND, 15000, false, 34000);

            assertThat(plan.totalAmount()).isEqualTo(15000);
            assertThat(plan.shippingFeeRefund()).isZero();
            assertThat(plan.faultParty()).isEqualTo(FaultParty.SELLER);
        }

        @Test
        void 일부_환불은_1원_미만이거나_환불_가능_잔액을_넘으면_거부한다() {
            Claim claim = returnClaim(ClaimReasonType.SELLER_FAULT, 30000);

            assertThatThrownBy(() -> claim.planReturnRefund(ReturnDecision.PARTIAL_REFUND, 0, false, 34000))
                    .isInstanceOf(InvalidRefundAmountException.class);
            assertThatThrownBy(() -> claim.planReturnRefund(ReturnDecision.PARTIAL_REFUND, -1, false, 34000))
                    .isInstanceOf(InvalidRefundAmountException.class);
            assertThatThrownBy(() -> claim.planReturnRefund(ReturnDecision.PARTIAL_REFUND, 34001, false, 34000))
                    .isInstanceOf(InvalidRefundAmountException.class);
            assertThatThrownBy(() -> claim.planReturnRefund(ReturnDecision.PARTIAL_REFUND, null, false, 34000))
                    .isInstanceOf(InvalidRefundAmountException.class);
        }

        @Test
        void 일부_환불은_잔액과_같은_금액까지_허용한다() {
            Claim claim = returnClaim(ClaimReasonType.SELLER_FAULT, 30000);

            ReturnRefundPlan plan = claim.planReturnRefund(ReturnDecision.PARTIAL_REFUND, 34000, false, 34000);

            assertThat(plan.totalAmount()).isEqualTo(34000);
        }

        @Test
        void 판매자_귀책_귀책_아님은_변심과_같이_배송비를_차감한다() {
            Claim claim = returnClaim(ClaimReasonType.SELLER_FAULT, 30000);

            ReturnRefundPlan plan = claim.planReturnRefund(ReturnDecision.NOT_SELLER_FAULT, null, false, 34000);

            assertThat(plan.totalAmount()).isEqualTo(26000);
            assertThat(plan.faultParty()).isEqualTo(FaultParty.BUYER);
        }

        @Test
        void 판매자_귀책은_결정_없이_승인할_수_없다() {
            Claim claim = returnClaim(ClaimReasonType.SELLER_FAULT, 30000);

            assertThatThrownBy(() -> claim.planReturnRefund(null, null, false, 34000))
                    .isInstanceOf(InvalidRefundDecisionException.class);
        }

        @Test
        void 차감하면_1원_미만이_되는_변심_반품은_환불_금액_오류다() {
            Claim claim = returnClaim(ClaimReasonType.CHANGE_OF_MIND, 4000);

            assertThatThrownBy(() -> claim.planReturnRefund(null, null, false, 8000))
                    .isInstanceOf(InvalidRefundAmountException.class);
        }

        @Test
        void 처리된_반품은_환불액을_계산할_수_없다() {
            Claim claim = returnClaim(ClaimReasonType.CHANGE_OF_MIND, 30000);
            claim.reject();

            assertThatThrownBy(() -> claim.planReturnRefund(null, null, false, 34000))
                    .isInstanceOf(CannotRefundException.class);
        }
    }
}
