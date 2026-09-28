package com.mavis.api.order.service;

import com.mavis.api.support.ControllerTestSupport;
import com.mavis.domain.domains.claim.domain.Claim;
import com.mavis.domain.domains.claim.domain.ClaimItem;
import com.mavis.domain.domains.claim.domain.ClaimStatus;
import com.mavis.domain.domains.claim.domain.ClaimType;
import com.mavis.domain.domains.claim.repository.ClaimRepository;
import com.mavis.domain.domains.order.domain.*;
import com.mavis.domain.domains.order.repository.OrderItemRepository;
import com.mavis.domain.domains.order.repository.OrderRepository;
import com.mavis.domain.domains.order.repository.PaymentIdempotencyRepository;
import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.product.repository.ProductRepository;
import com.mavis.domain.domains.refund.domain.Refund;
import com.mavis.domain.domains.refund.repository.RefundRepository;
import com.mavis.domain.domains.user.domain.SnsType;
import com.mavis.domain.domains.user.domain.User;
import com.mavis.domain.domains.user.repository.UserRepository;
import com.mavis.infrastructure.outer.api.tosspayments.dto.PaymentsCancels;
import com.mavis.infrastructure.outer.api.tosspayments.dto.PaymentsResponse;
import com.mavis.infrastructure.outer.api.tosspayments.dto.PaymentsStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderCancelClaimTest extends ControllerTestSupport {

    @Autowired private OrderService orderService;
    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private PaymentIdempotencyRepository paymentIdempotencyRepository;
    @Autowired private ClaimRepository claimRepository;
    @Autowired private RefundRepository refundRepository;
    @Autowired private EntityManager em;

    @Test
    void 주문취소_후처리는_클레임_1건과_배송비를_포함한_환불_1행을_만든다() {
        User user = userRepository.save(User.builder().snsType(SnsType.KAKAO).name("취소유저").build());
        Product product = productRepository.save(Product.builder().name("상품").price(10000).build());
        Order order = orderRepository.save(Order.builder()
                .orderId("GARAM-CANCEL-1")
                .user(user)
                .totalPrice(34000)
                .deliveryFee(4000)
                .orderStatus(OrderStatus.PAYMENT_CONFIRMED)
                .orderAddress(new OrderAddress("홍길동", "010-1234-5678", "12345", "서울시", "101호", ""))
                .build());
        OrderItem item1 = orderItemRepository.save(OrderItem.builder()
                .order(order).product(product).color("블랙").quantity(1).totalPrice(10000).build());
        OrderItem item2 = orderItemRepository.save(OrderItem.builder()
                .order(order).product(product).color("화이트").quantity(2).totalPrice(20000).build());
        PaymentIdempotency idempotency = paymentIdempotencyRepository.save(PaymentIdempotency.ofProcessing(
                "cancel-claim-test", PaymentApiType.CANCEL, LocalDateTime.now().plusDays(15)));
        em.flush();
        em.clear();

        PaymentsCancels cancelEntry = new PaymentsCancels(34000, "단순 변심", null, null, null, null, OffsetDateTime.now(), "tx-cancel-1");
        PaymentsResponse response = new PaymentsResponse(
                null, "payKey", null, "GARAM-CANCEL-1", null, null, null,
                null, 34000, 0L, PaymentsStatus.CANCELED,
                OffsetDateTime.now(), null, null, "tx-cancel-1", null, null,
                null, null, null, List.of(cancelEntry), null, null, null, null,
                null, null, null, null, null, null, null
        );

        orderService.processCancelSuccess(order.getId(), response, cancelEntry, "단순 변심", idempotency.getId());
        em.flush();
        em.clear();

        List<Claim> claims = claimRepository.findAll();
        assertThat(claims).hasSize(1);
        Claim claim = claims.get(0);
        assertThat(claim.getClaimType()).isEqualTo(ClaimType.CANCEL);
        assertThat(claim.getClaimStatus()).isEqualTo(ClaimStatus.COMPLETED);
        assertThat(claim.getReason()).isEqualTo("단순 변심");
        assertThat(claim.getItems()).extracting(ClaimItem::getOrderItem)
                .extracting(OrderItem::getId)
                .containsExactlyInAnyOrder(item1.getId(), item2.getId());

        List<Refund> refunds = refundRepository.findAll();
        assertThat(refunds).hasSize(1);
        Refund refund = refunds.get(0);
        assertThat(refund.getTotalAmount()).isEqualTo(34000);
        assertThat(refund.getProductAmount()).isEqualTo(30000);
        assertThat(refund.getShippingFeeRefund()).isEqualTo(4000);
        assertThat(refund.getCancelTransactionKey()).isEqualTo("tx-cancel-1");

        Order canceled = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(canceled.getOrderStatus()).isEqualTo(OrderStatus.CANCELED);
    }
}
