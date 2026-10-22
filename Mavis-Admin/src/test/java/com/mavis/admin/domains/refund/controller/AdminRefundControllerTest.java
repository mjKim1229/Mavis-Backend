package com.mavis.admin.domains.refund.controller;

import com.mavis.admin.domains.refund.dto.RefundValidateInfo;
import com.mavis.admin.domains.refund.service.AdminRefundService;
import com.mavis.admin.support.ControllerTestSupport;
import com.mavis.domain.domains.admin.domain.Admin;
import com.mavis.domain.domains.admin.repository.AdminRepository;
import com.mavis.domain.domains.claim.domain.Claim;
import com.mavis.domain.domains.claim.domain.ClaimReasonType;
import com.mavis.domain.domains.claim.domain.ReturnItemReason;
import com.mavis.domain.domains.claim.domain.ClaimReturn;
import com.mavis.domain.domains.claim.domain.ClaimStatus;
import com.mavis.domain.domains.claim.domain.FaultParty;
import com.mavis.domain.domains.claim.domain.ReturnDecision;
import com.mavis.domain.domains.claim.domain.ReturnRefundPlan;
import com.mavis.domain.domains.claim.repository.ClaimRepository;
import com.mavis.domain.domains.claim.repository.ClaimReturnRepository;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderAddress;
import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.order.domain.OrderStatus;
import com.mavis.domain.domains.order.domain.Payment;
import com.mavis.domain.domains.order.domain.PaymentMethod;
import com.mavis.domain.domains.order.domain.PaymentType;
import com.mavis.domain.domains.order.repository.OrderItemRepository;
import com.mavis.domain.domains.order.repository.OrderRepository;
import com.mavis.domain.domains.order.repository.PaymentRepository;
import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.product.repository.ProductRepository;
import com.mavis.domain.domains.refund.domain.Refund;
import com.mavis.domain.domains.refund.exception.InvalidRefundAmountException;
import com.mavis.domain.domains.refund.repository.RefundRepository;
import com.mavis.domain.domains.user.domain.User;
import com.mavis.domain.domains.user.repository.UserRepository;
import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminRefundControllerTest extends ControllerTestSupport {

    @Autowired private AdminRepository adminRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private ClaimRepository claimRepository;
    @Autowired private ClaimReturnRepository claimReturnRepository;
    @Autowired private RefundRepository refundRepository;
    @Autowired private AdminRefundService adminRefundService;
    @Autowired private EntityManager em;

    private Admin savedAdmin;
    private User savedUser;
    private Product savedProduct;

    @BeforeEach
    void setUp() {
        savedAdmin = adminRepository.save(Admin.builder()
                .username("admin")
                .password("password")
                .build());
        savedUser = userRepository.save(User.builder()
                .name("홍길동")
                .email("hong@test.com")
                .build());
        savedProduct = productRepository.save(Product.builder()
                .name("테스트상품")
                .price(50000)
                .build());
    }

    private Order createOrder(String orderId, int totalPrice) {
        return orderRepository.save(Order.builder()
                .orderId(orderId)
                .user(savedUser)
                .totalPrice(totalPrice)
                .deliveryFee(4000)
                .orderStatus(OrderStatus.ORDERED)
                .orderAddress(new OrderAddress("수신자", "010-1234-5678", "12345", "서울시 강남구", "101호", "문앞에 놔주세요"))
                .build());
    }

    private OrderItem createOrderItem(Order order, String color, int quantity) {
        return orderItemRepository.save(OrderItem.builder()
                .order(order)
                .product(savedProduct)
                .color(color)
                .quantity(quantity)
                .totalPrice(savedProduct.getPrice())
                .build());
    }

    private Claim createReturnClaim(OrderItem orderItem, ClaimStatus status, String reason, String... imageUrls) {
        Claim claim = Claim.requestReturn(orderItem.getOrder(), ClaimReasonType.CHANGE_OF_MIND, List.of(new ReturnItemReason(orderItem, reason)));
        for (String imageUrl : imageUrls) {
            claim.addImage(imageUrl);
        }
        if (status == ClaimStatus.REJECTED) {
            claim.reject();
        }
        if (status == ClaimStatus.COMPLETED) {
            claim.complete(FaultParty.BUYER);
        }
        claimRepository.save(claim);
        claimReturnRepository.save(ClaimReturn.of(claim, "CJ대한통운", "1234567890"));
        return claim;
    }

    private Payment createCancelPayment(Order order, int cancelAmount) {
        return paymentRepository.save(Payment.builder()
                .order(order)
                .paymentType(PaymentType.CANCEL)
                .method(PaymentMethod.CARD)
                .totalAmount(cancelAmount)
                .cancelAmount(cancelAmount)
                .lastTransactionKey("tx-" + order.getOrderId())
                .build());
    }

    @Nested
    class 환불_목록_조회 {

        @Test
        void 조회_성공_JSON_필드_전체_검증() throws Exception {
            Order order = createOrder("GARAM20240101ABCD", 50000);
            OrderItem orderItem = createOrderItem(order, "블랙", 2);
            Claim claim = createReturnClaim(orderItem, ClaimStatus.REQUESTED, "단순 변심");
            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/refund")
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.content[0].orderInfo.tossOrderId").value("20240101ABCD"))
                    .andExpect(jsonPath("$.data.content[0].orderInfo.receiverName").value("수신자"))
                    .andExpect(jsonPath("$.data.content[0].orderInfo.receiverPhoneNumber").value("010-1234-5678"))
                    .andExpect(jsonPath("$.data.content[0].orderInfo.orderItemId").value(orderItem.getId()))
                    .andExpect(jsonPath("$.data.content[0].orderInfo.orderItemInfo.productName").value("테스트상품"))
                    .andExpect(jsonPath("$.data.content[0].orderInfo.orderItemInfo.color").value("블랙"))
                    .andExpect(jsonPath("$.data.content[0].orderInfo.orderItemInfo.quantity").value(2))
                    .andExpect(jsonPath("$.data.content[0].refundInfo.refundId").value(claim.getId()))
                    // 승인 전이라 환불 예정액 = 상품 금액 - 주문 배송비 4000 - 반품 배송비 4000
                    .andExpect(jsonPath("$.data.content[0].refundInfo.refundAmount").value(42000))
                    .andExpect(jsonPath("$.data.content[0].refundInfo.fullRefundAmount").value(54000))
                    .andExpect(jsonPath("$.data.content[0].refundInfo.reasonType").value("CHANGE_OF_MIND"))
                    .andExpect(jsonPath("$.data.content[0].orderInfo.orderItems.length()").value(1))
                    .andExpect(jsonPath("$.data.content[0].orderInfo.orderItems[0].orderItemId").value(orderItem.getId()))
                    .andExpect(jsonPath("$.data.content[0].refundInfo.refundReason").value("단순 변심"))
                    .andExpect(jsonPath("$.data.content[0].refundInfo.refundStatus").value("REQUESTED"))
                    .andExpect(jsonPath("$.data.content[0].refundInfo.carrier").value("CJ대한통운"))
                    .andExpect(jsonPath("$.data.content[0].refundInfo.trackingNumber").value("1234567890"))
                    .andExpect(jsonPath("$.data.content[0].refundInfo.requestedAt").isNotEmpty());
        }

        @Test
        void 완료된_반품은_실제_환불액을_반환() throws Exception {
            Order order = createOrder("GARAM20240101PAID", 50000);
            OrderItem orderItem = createOrderItem(order, "블랙", 1);
            Claim claim = createReturnClaim(orderItem, ClaimStatus.COMPLETED, "단순 변심");
            Payment cancelPayment = createCancelPayment(order, 45000);
            refundRepository.save(Refund.forReturn(claim, cancelPayment, new ReturnRefundPlan(45000, 0, 0, 0, FaultParty.BUYER)));
            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/refund")
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].refundInfo.refundStatus").value("COMPLETED"))
                    .andExpect(jsonPath("$.data.content[0].refundInfo.refundAmount").value(45000));
        }

        @Test
        void refundStatus_파라미터로_상태별_필터링() throws Exception {
            Order order = createOrder("GARAM20240101FILT", 50000);
            OrderItem item1 = createOrderItem(order, "블랙", 1);
            OrderItem item2 = createOrderItem(order, "화이트", 1);
            OrderItem item3 = createOrderItem(order, "레드", 1);
            createReturnClaim(item1, ClaimStatus.REQUESTED, "요청1");
            createReturnClaim(item2, ClaimStatus.REJECTED, "거절1");
            createReturnClaim(item3, ClaimStatus.COMPLETED, "완료1");
            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/refund")
                            .param("refundStatus", "REQUESTED")
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.content[0].refundInfo.refundStatus").value("REQUESTED"));
        }

        @Test
        void refundStatus_없으면_전체_조회() throws Exception {
            Order order = createOrder("GARAM20240101ALLL", 50000);
            OrderItem item1 = createOrderItem(order, "블랙", 1);
            OrderItem item2 = createOrderItem(order, "화이트", 1);
            createReturnClaim(item1, ClaimStatus.REQUESTED, "요청1");
            createReturnClaim(item2, ClaimStatus.REJECTED, "거절1");
            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/refund")
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(2));
        }

        @Test
        void 주문취소_클레임은_반품_목록에_나오지_않는다() throws Exception {
            Order order = createOrder("GARAM20240101CNCL", 54000);
            OrderItem orderItem = createOrderItem(order, "블랙", 1);
            claimRepository.save(Claim.cancel(order, List.of(orderItem), "취소"));
            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/refund")
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(0));
        }

        @Test
        void 환불_이미지_여러개_imageUrls로_조회() throws Exception {
            Order order = createOrder("GARAM20240101IMGS", 50000);
            OrderItem orderItem = createOrderItem(order, "블랙", 1);
            createReturnClaim(orderItem, ClaimStatus.REQUESTED, "이미지 포함",
                    "https://cdn.test/refund1.jpg", "https://cdn.test/refund2.jpg");
            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/refund")
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.content[0].refundInfo.imageUrls.length()").value(2))
                    .andExpect(jsonPath("$.data.content[0].refundInfo.imageUrls[0]").value("https://cdn.test/refund1.jpg"))
                    .andExpect(jsonPath("$.data.content[0].refundInfo.imageUrls[1]").value("https://cdn.test/refund2.jpg"));
        }

        @Test
        void 환불_이미지_없으면_빈_imageUrls() throws Exception {
            Order order = createOrder("GARAM20240101NOIM", 50000);
            OrderItem orderItem = createOrderItem(order, "블랙", 1);
            createReturnClaim(orderItem, ClaimStatus.REQUESTED, "이미지 없음");
            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/refund")
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].refundInfo.imageUrls").isArray())
                    .andExpect(jsonPath("$.data.content[0].refundInfo.imageUrls").isEmpty());
        }

        @Test
        void 환불_없을때_빈_목록_반환() throws Exception {
            mockMvc.perform(get("/v1/api/refund")
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(0))
                    .andExpect(jsonPath("$.data.content").isEmpty());
        }

        @Test
        void 비인증_요청시_401() throws Exception {
            mockMvc.perform(get("/v1/api/refund"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class 주문취소_목록_조회 {

        @Test
        void 상품_단위_행으로_조회되고_금액은_취소_총액이다() throws Exception {
            Order order = createOrder("GARAM20240303CNCL", 104000);
            OrderItem item1 = createOrderItem(order, "블랙", 1);
            OrderItem item2 = createOrderItem(order, "화이트", 1);
            Claim claim = Claim.cancel(order, List.of(item1, item2), "단순 변심");
            claimRepository.save(claim);
            Payment cancelPayment = createCancelPayment(order, 104000);
            refundRepository.save(Refund.forCancel(claim, cancelPayment, 4000));
            em.flush();
            em.clear();

            String body = mockMvc.perform(get("/v1/api/refund/canceled")
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(2))
                    .andExpect(jsonPath("$.data.content[0].orderInfo.tossOrderId").value("20240303CNCL"))
                    .andExpect(jsonPath("$.data.content[0].cancelInfo.refundAmount").value(104000))
                    .andExpect(jsonPath("$.data.content[0].cancelInfo.cancelReason").value("단순 변심"))
                    .andExpect(jsonPath("$.data.content[0].cancelInfo.paymentMethod").value("CARD"))
                    .andExpect(jsonPath("$.data.content[1].cancelInfo.refundAmount").value(104000))
                    .andReturn().getResponse().getContentAsString();

            // FE가 행 key로 쓰므로 행마다 달라야 한다
            List<Integer> rowIds = JsonPath.read(body, "$.data.content[*].cancelInfo.refundId");
            assertThat(rowIds).doesNotHaveDuplicates();
        }
    }

    private Claim createSellerFaultClaim(OrderItem orderItem) {
        Claim claim = Claim.requestReturn(orderItem.getOrder(), ClaimReasonType.SELLER_FAULT, List.of(new ReturnItemReason(orderItem, "불량")));
        return claimRepository.save(claim);
    }

    private Payment createConfirmPayment(Order order, int amount) {
        return paymentRepository.save(Payment.builder()
                .order(order)
                .paymentType(PaymentType.CONFIRM)
                .method(PaymentMethod.CARD)
                .paymentKey("pk-" + order.getOrderId())
                .totalAmount(amount)
                .build());
    }

    @Nested
    class 환불_승인_금액_계산 {

        @Test
        void 변심_첫_반품은_배송비_8000원을_차감한다() {
            Order order = createOrder("GARAM20240505FRST", 54000);
            OrderItem orderItem = createOrderItem(order, "블랙", 1);
            Claim claim = createReturnClaim(orderItem, ClaimStatus.REQUESTED, "단순 변심");
            createConfirmPayment(order, 54000);
            em.flush();
            em.clear();

            RefundValidateInfo info = adminRefundService.validateForApproval(claim.getId(), null, null);

            assertThat(info.refundAmount()).isEqualTo(42000);
            assertThat(info.plan().shippingFeeRefund()).isZero();
            assertThat(info.plan().orderShippingFeeDeducted()).isEqualTo(4000);
            assertThat(info.plan().returnShippingFeeDeducted()).isEqualTo(4000);
            assertThat(info.plan().faultParty()).isEqualTo(FaultParty.BUYER);
        }

        @Test
        void 같은_주문에서_먼저_완료된_반품이_있으면_반품_배송비만_차감한다() {
            Order order = createOrder("GARAM20240505NEXT", 104000);
            OrderItem item1 = createOrderItem(order, "블랙", 1);
            OrderItem item2 = createOrderItem(order, "화이트", 1);
            Claim first = createReturnClaim(item1, ClaimStatus.COMPLETED, "단순 변심");
            createConfirmPayment(order, 104000);
            Payment cancelPayment = createCancelPayment(order, 42000);
            refundRepository.save(Refund.forReturn(first, cancelPayment, new ReturnRefundPlan(42000, 0, 4000, 4000, FaultParty.BUYER)));
            Claim second = createReturnClaim(item2, ClaimStatus.REQUESTED, "단순 변심");
            em.flush();
            em.clear();

            RefundValidateInfo info = adminRefundService.validateForApproval(second.getId(), null, null);

            assertThat(info.refundAmount()).isEqualTo(46000);
            assertThat(info.plan().orderShippingFeeDeducted()).isZero();
            assertThat(info.plan().returnShippingFeeDeducted()).isEqualTo(4000);
        }

        @Test
        void 거절된_반품은_배송비_정산으로_보지_않는다() {
            Order order = createOrder("GARAM20240505RJCT", 104000);
            OrderItem item1 = createOrderItem(order, "블랙", 1);
            OrderItem item2 = createOrderItem(order, "화이트", 1);
            createReturnClaim(item1, ClaimStatus.REJECTED, "단순 변심");
            Claim second = createReturnClaim(item2, ClaimStatus.REQUESTED, "단순 변심");
            createConfirmPayment(order, 104000);
            em.flush();
            em.clear();

            RefundValidateInfo info = adminRefundService.validateForApproval(second.getId(), null, null);

            assertThat(info.refundAmount()).isEqualTo(42000);
        }

        @Test
        void 판매자_귀책_전액_환불은_상품금액과_주문_배송비를_포함한다() {
            Order order = createOrder("GARAM20240505FULL", 54000);
            OrderItem orderItem = createOrderItem(order, "블랙", 1);
            Claim claim = createSellerFaultClaim(orderItem);
            createConfirmPayment(order, 54000);
            em.flush();
            em.clear();

            RefundValidateInfo info = adminRefundService.validateForApproval(claim.getId(), ReturnDecision.FULL_REFUND, null);

            assertThat(info.refundAmount()).isEqualTo(54000);
            assertThat(info.plan().shippingFeeRefund()).isEqualTo(4000);
            assertThat(info.plan().orderShippingFeeDeducted()).isZero();
            assertThat(info.plan().returnShippingFeeDeducted()).isZero();
            assertThat(info.plan().faultParty()).isEqualTo(FaultParty.SELLER);
        }

        @Test
        void 일부_환불_금액이_환불_가능_잔액을_넘으면_거부한다() {
            Order order = createOrder("GARAM20240505OVER", 104000);
            OrderItem item1 = createOrderItem(order, "블랙", 1);
            OrderItem item2 = createOrderItem(order, "화이트", 1);
            Claim first = createReturnClaim(item1, ClaimStatus.COMPLETED, "단순 변심");
            createConfirmPayment(order, 104000);
            Payment cancelPayment = createCancelPayment(order, 42000);
            refundRepository.save(Refund.forReturn(first, cancelPayment, new ReturnRefundPlan(42000, 0, 4000, 4000, FaultParty.BUYER)));
            Claim second = createSellerFaultClaim(item2);
            em.flush();
            em.clear();

            RefundValidateInfo atLimit = adminRefundService.validateForApproval(second.getId(), ReturnDecision.PARTIAL_REFUND, 62000);

            assertThat(atLimit.refundAmount()).isEqualTo(62000);
            assertThatThrownBy(() -> adminRefundService.validateForApproval(second.getId(), ReturnDecision.PARTIAL_REFUND, 62001))
                    .isInstanceOf(InvalidRefundAmountException.class);
        }

        @Test
        void 승인_API는_변심에_전액_환불_결정을_보내면_400() throws Exception {
            Order order = createOrder("GARAM20240505BADD", 54000);
            OrderItem orderItem = createOrderItem(order, "블랙", 1);
            Claim claim = createReturnClaim(orderItem, ClaimStatus.REQUESTED, "단순 변심");
            createConfirmPayment(order, 54000);
            em.flush();
            em.clear();

            mockMvc.perform(post("/v1/api/refund/{claimId}/approve", claim.getId())
                            .header("Idempotency-Key", "key-bad-decision")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"decision\":\"FULL_REFUND\"}")
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void 승인_API는_판매자_귀책에_결정이_없으면_400() throws Exception {
            Order order = createOrder("GARAM20240505NODC", 54000);
            OrderItem orderItem = createOrderItem(order, "블랙", 1);
            Claim claim = createSellerFaultClaim(orderItem);
            createConfirmPayment(order, 54000);
            em.flush();
            em.clear();

            mockMvc.perform(post("/v1/api/refund/{claimId}/approve", claim.getId())
                            .header("Idempotency-Key", "key-no-decision")
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void 승인_API는_일부_환불_금액이_0원이면_400() throws Exception {
            Order order = createOrder("GARAM20240505ZERO", 54000);
            OrderItem orderItem = createOrderItem(order, "블랙", 1);
            Claim claim = createSellerFaultClaim(orderItem);
            createConfirmPayment(order, 54000);
            em.flush();
            em.clear();

            mockMvc.perform(post("/v1/api/refund/{claimId}/approve", claim.getId())
                            .header("Idempotency-Key", "key-zero-amount")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"decision\":\"PARTIAL_REFUND\",\"amount\":0}")
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class 환불_거절 {

        @Test
        void 성공_환불상태_REJECTED로_변경() throws Exception {
            Order order = createOrder("GARAM20240202REJT", 50000);
            OrderItem orderItem = createOrderItem(order, "블랙", 1);
            Claim claim = createReturnClaim(orderItem, ClaimStatus.REQUESTED, "단순 변심");
            em.flush();
            em.clear();

            mockMvc.perform(post("/v1/api/refund/{claimId}/reject", claim.getId())
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk());

            em.flush();
            em.clear();

            Claim updated = claimRepository.findById(claim.getId()).orElseThrow();
            assertThat(updated.getClaimStatus()).isEqualTo(ClaimStatus.REJECTED);
            assertThat(updated.getCompletedAt()).isNotNull();
        }

        @Test
        void 이미_거절된_환불_재거절시_400() throws Exception {
            Order order = createOrder("GARAM20240202DONE", 50000);
            OrderItem orderItem = createOrderItem(order, "블랙", 1);
            Claim claim = createReturnClaim(orderItem, ClaimStatus.REJECTED, "단순 변심");
            em.flush();
            em.clear();

            mockMvc.perform(post("/v1/api/refund/{claimId}/reject", claim.getId())
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void 이미_완료된_환불_거절시_400() throws Exception {
            Order order = createOrder("GARAM20240202COMP", 50000);
            OrderItem orderItem = createOrderItem(order, "블랙", 1);
            Claim claim = createReturnClaim(orderItem, ClaimStatus.COMPLETED, "단순 변심");
            em.flush();
            em.clear();

            mockMvc.perform(post("/v1/api/refund/{claimId}/reject", claim.getId())
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void 주문취소_클레임_거절시_400() throws Exception {
            Order order = createOrder("GARAM20240202CNCL", 54000);
            OrderItem orderItem = createOrderItem(order, "블랙", 1);
            Claim claim = Claim.cancel(order, List.of(orderItem), "취소");
            claimRepository.save(claim);
            em.flush();
            em.clear();

            mockMvc.perform(post("/v1/api/refund/{claimId}/reject", claim.getId())
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void 존재하지_않는_환불_거절시_404() throws Exception {
            mockMvc.perform(post("/v1/api/refund/{claimId}/reject", 999999L)
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isNotFound());
        }

        @Test
        void 비인증_요청시_401() throws Exception {
            mockMvc.perform(post("/v1/api/refund/{claimId}/reject", 1L))
                    .andExpect(status().isUnauthorized());
        }
    }
}
