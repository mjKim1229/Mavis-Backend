package com.mavis.domain.domains.refund.domain;

import com.mavis.domain.domains.claim.domain.Claim;
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
    void 반품_환불은_배송비_없이_취소금액_전부가_상품금액이다() {
        OrderItem item = OrderItem.builder().order(order).totalPrice(20000).build();
        Claim claim = Claim.requestReturn(item, "변심");

        Refund refund = Refund.forReturn(claim, cancelPayment(20000));

        assertThat(refund.getTotalAmount()).isEqualTo(20000);
        assertThat(refund.getProductAmount()).isEqualTo(20000);
        assertThat(refund.getShippingFeeRefund()).isZero();
        assertThat(refund.getTotalAmount()).isEqualTo(refund.getProductAmount() + refund.getShippingFeeRefund());
    }
}
