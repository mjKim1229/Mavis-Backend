package com.mavis.domain.domains.refund.domain;

import com.mavis.domain.domains.claim.domain.Claim;
import com.mavis.domain.domains.claim.domain.ClaimReasonType;
import com.mavis.domain.domains.claim.domain.FaultParty;
import com.mavis.domain.domains.claim.domain.ReturnItemReason;
import com.mavis.domain.domains.claim.domain.ReturnRefundPlan;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.order.domain.Payment;
import com.mavis.domain.domains.order.domain.PaymentType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RefundTest {

    private final Order order = Order.builder().build();

    private Payment cancelPayment(int cancelAmount) {
        return Payment.builder()
                .order(order)
                .paymentType(PaymentType.CANCEL)
                .cancelAmount(cancelAmount)
                .lastTransactionKey("tx-cancel")
                .build();
    }

    @Test
    void 주문취소_환불은_토스_취소금액을_상품과_배송비로_나눈다() {
        OrderItem item = OrderItem.builder().order(order).totalPrice(30000).build();
        Claim claim = Claim.cancel(order, List.of(item), "변심");

        Refund refund = Refund.forCancel(claim, cancelPayment(34000), 4000);

        assertThat(refund.getTotalAmount()).isEqualTo(34000);
        assertThat(refund.getProductAmount()).isEqualTo(30000);
        assertThat(refund.getShippingFeeRefund()).isEqualTo(4000);
        assertThat(refund.getCancelTransactionKey()).isEqualTo("tx-cancel");
    }

    @Test
    void 변심_반품_환불은_차감한_배송비를_스냅샷으로_기록한다() {
        OrderItem item = OrderItem.builder().order(order).totalPrice(30000).build();
        Claim claim = Claim.requestReturn(item.getOrder(), ClaimReasonType.CHANGE_OF_MIND, List.of(new ReturnItemReason(item, "변심")));
        ReturnRefundPlan plan = new ReturnRefundPlan(26000, 0, 4000, 4000, FaultParty.BUYER);

        Refund refund = Refund.forReturn(claim, cancelPayment(26000), plan);

        assertThat(refund.getTotalAmount()).isEqualTo(26000);
        assertThat(refund.getProductAmount()).isEqualTo(26000);
        assertThat(refund.getShippingFeeRefund()).isZero();
        assertThat(refund.getOrderShippingFeeDeducted()).isEqualTo(4000);
        assertThat(refund.getReturnShippingFeeDeducted()).isEqualTo(4000);
    }

    @Test
    void 판매자_귀책_전액_환불은_주문_배송비를_배송비_환불로_기록하고_차감은_없다() {
        OrderItem item = OrderItem.builder().order(order).totalPrice(30000).build();
        Claim claim = Claim.requestReturn(item.getOrder(), ClaimReasonType.SELLER_FAULT, List.of(new ReturnItemReason(item, "불량")));
        ReturnRefundPlan plan = new ReturnRefundPlan(34000, 4000, 0, 0, FaultParty.SELLER);

        Refund refund = Refund.forReturn(claim, cancelPayment(34000), plan);

        assertThat(refund.getTotalAmount()).isEqualTo(34000);
        assertThat(refund.getProductAmount()).isEqualTo(30000);
        assertThat(refund.getShippingFeeRefund()).isEqualTo(4000);
        assertThat(refund.getOrderShippingFeeDeducted()).isZero();
        assertThat(refund.getReturnShippingFeeDeducted()).isZero();
    }
}
