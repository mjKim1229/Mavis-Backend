package com.mavis.admin.domains.order.controller;

import com.mavis.admin.domains.order.dto.AdminOrderConfirmRequest;
import com.mavis.admin.support.ControllerTestSupport;
import com.mavis.domain.domains.admin.domain.Admin;
import com.mavis.domain.domains.admin.repository.AdminRepository;
import com.mavis.domain.domains.delivery.domain.Delivery;
import com.mavis.domain.domains.delivery.domain.DeliveryStatus;
import com.mavis.domain.domains.delivery.repository.DeliveryRepository;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderAddress;
import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.order.domain.OrderStatus;
import com.mavis.domain.domains.order.repository.OrderItemRepository;
import com.mavis.domain.domains.order.repository.OrderRepository;
import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.product.repository.ProductRepository;
import com.mavis.domain.domains.user.domain.User;
import com.mavis.domain.domains.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminOrderControllerTest extends ControllerTestSupport {

    @Autowired private AdminRepository adminRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private DeliveryRepository deliveryRepository;
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

    private Order createOrder(String orderId, OrderStatus status, int totalPrice) {
        return orderRepository.save(Order.builder()
                .orderId(orderId)
                .user(savedUser)
                .totalPrice(totalPrice)
                .orderStatus(status)
                .orderAddress(new OrderAddress("수신자", "010-1234-5678", "12345", "서울시 강남구", "101호", "문앞에 놔주세요"))
                .build());
    }

    private void createOrderItem(Order order, String color, int quantity) {
        orderItemRepository.save(OrderItem.builder()
                .order(order)
                .product(savedProduct)
                .color(color)
                .quantity(quantity)
                .price(savedProduct.getPrice())
                .build());
    }

    @Nested
    class 결제완료_주문_목록_조회 {

        @Test
        void 조회_성공_JSON_필드_전체_검증() throws Exception {
            Order order = createOrder("GARAM20240101ABCD", OrderStatus.PAYMENT_CONFIRMED, 50000);
            createOrderItem(order, "블랙", 2);
            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/order/payment-confirmed")
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.content[0].orderId").value(order.getId()))
                    .andExpect(jsonPath("$.data.content[0].tossOrderId").value("20240101ABCD"))
                    .andExpect(jsonPath("$.data.content[0].receiverName").value("수신자"))
                    .andExpect(jsonPath("$.data.content[0].receiverPhoneNumber").value("010-1234-5678"))
                    .andExpect(jsonPath("$.data.content[0].address").value("서울시 강남구"))
                    .andExpect(jsonPath("$.data.content[0].requestMessage").value("문앞에 놔주세요"))
                    .andExpect(jsonPath("$.data.content[0].buyerName").value("홍길동"))
                    .andExpect(jsonPath("$.data.content[0].totalPrice").value(50000))
                    .andExpect(jsonPath("$.data.content[0].orderedAt").isNotEmpty())
                    .andExpect(jsonPath("$.data.content[0].orderItemInfos").isArray())
                    .andExpect(jsonPath("$.data.content[0].orderItemInfos[0].productName").value("테스트상품"))
                    .andExpect(jsonPath("$.data.content[0].orderItemInfos[0].color").value("블랙"))
                    .andExpect(jsonPath("$.data.content[0].orderItemInfos[0].quantity").value(2));
        }

        @Test
        void 주문상품_여러개일때_전체_포함() throws Exception {
            Order order = createOrder("GARAM20240101MULTI", OrderStatus.PAYMENT_CONFIRMED, 100000);
            createOrderItem(order, "블랙", 1);
            createOrderItem(order, "화이트", 2);
            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/order/payment-confirmed")
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].orderItemInfos.length()").value(2));
        }

        @Test
        void PAYMENT_CONFIRMED_상태_주문만_포함() throws Exception {
            createOrder("GARAM20240101AAAA", OrderStatus.PAYMENT_CONFIRMED, 50000);
            createOrder("GARAM20240101BBBB", OrderStatus.ORDERED, 30000);
            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/order/payment-confirmed")
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        void 삭제된_주문은_미포함() throws Exception {
            orderRepository.save(Order.builder()
                    .orderId("GARAM20240101DELD")
                    .user(savedUser)
                    .totalPrice(50000)
                    .orderStatus(OrderStatus.PAYMENT_CONFIRMED)
                    .orderAddress(new OrderAddress("수신자", "010-1234-5678", "12345", "서울시 강남구", "101호", ""))
                    .isDeleted(true)
                    .build());
            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/order/payment-confirmed")
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(0));
        }

        @Test
        void 주문_없을때_빈_목록_반환() throws Exception {
            mockMvc.perform(get("/v1/api/order/payment-confirmed")
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(0))
                    .andExpect(jsonPath("$.data.content").isEmpty());
        }

        @Test
        void 비인증_요청시_401() throws Exception {
            mockMvc.perform(get("/v1/api/order/payment-confirmed"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class 발주완료_주문_목록_조회 {

        @Test
        void 조회_성공_JSON_필드_전체_검증() throws Exception {
            Order order = createOrder("GARAM20240202EFGH", OrderStatus.ORDERED, 75000);
            createOrderItem(order, "레드", 3);
            deliveryRepository.save(Delivery.builder()
                    .order(order)
                    .deliveryStatus(DeliveryStatus.READY)
                    .build());
            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/order/ordered")
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.content[0].orderId").value(order.getId()))
                    .andExpect(jsonPath("$.data.content[0].tossOrderId").value("20240202EFGH"))
                    .andExpect(jsonPath("$.data.content[0].receiverName").value("수신자"))
                    .andExpect(jsonPath("$.data.content[0].receiverPhoneNumber").value("010-1234-5678"))
                    .andExpect(jsonPath("$.data.content[0].address").value("서울시 강남구"))
                    .andExpect(jsonPath("$.data.content[0].requestMessage").value("문앞에 놔주세요"))
                    .andExpect(jsonPath("$.data.content[0].buyerName").value("홍길동"))
                    .andExpect(jsonPath("$.data.content[0].totalPrice").value(75000))
                    .andExpect(jsonPath("$.data.content[0].orderedAt").isNotEmpty())
                    .andExpect(jsonPath("$.data.content[0].orderItemInfos").isArray())
                    .andExpect(jsonPath("$.data.content[0].orderItemInfos[0].productName").value("테스트상품"))
                    .andExpect(jsonPath("$.data.content[0].orderItemInfos[0].color").value("레드"))
                    .andExpect(jsonPath("$.data.content[0].orderItemInfos[0].quantity").value(3));
        }

        @Test
        void READY_배송_없는_ORDERED_주문은_미포함() throws Exception {
            createOrder("GARAM20240202XXXX", OrderStatus.ORDERED, 50000);
            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/order/ordered")
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(0));
        }

        @Test
        void SHIPPED_배송_주문은_미포함() throws Exception {
            Order order = createOrder("GARAM20240202YYYY", OrderStatus.ORDERED, 50000);
            deliveryRepository.save(Delivery.builder()
                    .order(order)
                    .deliveryStatus(DeliveryStatus.SHIPPED)
                    .build());
            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/order/ordered")
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(0));
        }

        @Test
        void PAYMENT_CONFIRMED_주문은_미포함() throws Exception {
            createOrder("GARAM20240202ZZZZ", OrderStatus.PAYMENT_CONFIRMED, 50000);
            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/order/ordered")
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(0));
        }

        @Test
        void 주문_없을때_빈_목록_반환() throws Exception {
            mockMvc.perform(get("/v1/api/order/ordered")
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(0))
                    .andExpect(jsonPath("$.data.content").isEmpty());
        }

        @Test
        void 비인증_요청시_401() throws Exception {
            mockMvc.perform(get("/v1/api/order/ordered"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class 주문_카운트_조회 {

        @Test
        void 각_상태별_카운트_정확히_반환() throws Exception {
            // paymentConfirmedCount = 1
            createOrder("GARAMCNT001", OrderStatus.PAYMENT_CONFIRMED, 50000);

            // orderedCount = 2 (ORDERED + delivery READY)
            Order ordered1 = createOrder("GARAMCNT002", OrderStatus.ORDERED, 50000);
            deliveryRepository.save(Delivery.builder().order(ordered1).deliveryStatus(DeliveryStatus.READY).build());
            Order ordered2 = createOrder("GARAMCNT003", OrderStatus.ORDERED, 50000);
            deliveryRepository.save(Delivery.builder().order(ordered2).deliveryStatus(DeliveryStatus.READY).build());

            // ORDERED + SHIPPED → orderedCount 미포함, shippedCount 포함
            Order orderedShipped = createOrder("GARAMCNT004", OrderStatus.ORDERED, 50000);
            deliveryRepository.save(Delivery.builder().order(orderedShipped).deliveryStatus(DeliveryStatus.SHIPPED).build());

            // deliveredCount = 1
            Order orderedDelivered = createOrder("GARAMCNT005", OrderStatus.ORDERED, 50000);
            deliveryRepository.save(Delivery.builder().order(orderedDelivered).deliveryStatus(DeliveryStatus.DELIVERED).build());

            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/order/counts")
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.paymentConfirmedCount").value(1))
                    .andExpect(jsonPath("$.data.orderedCount").value(2))
                    .andExpect(jsonPath("$.data.shippedCount").value(1))
                    .andExpect(jsonPath("$.data.deliveredCount").value(1))
                    .andExpect(jsonPath("$.data.refundRequestedCount").value(0));
        }

        @Test
        void 데이터_없으면_모두_0() throws Exception {
            mockMvc.perform(get("/v1/api/order/counts")
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.paymentConfirmedCount").value(0))
                    .andExpect(jsonPath("$.data.orderedCount").value(0))
                    .andExpect(jsonPath("$.data.shippedCount").value(0))
                    .andExpect(jsonPath("$.data.deliveredCount").value(0))
                    .andExpect(jsonPath("$.data.refundRequestedCount").value(0));
        }

        @Test
        void 비인증_요청시_401() throws Exception {
            mockMvc.perform(get("/v1/api/order/counts"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class 발주_처리 {

        @Test
        void 성공_주문상태_ORDERED로_변경_및_배송_READY_생성() throws Exception {
            Order order1 = createOrder("GARAMCONF001", OrderStatus.PAYMENT_CONFIRMED, 50000);
            Order order2 = createOrder("GARAMCONF002", OrderStatus.PAYMENT_CONFIRMED, 30000);
            em.flush();
            em.clear();

            mockMvc.perform(post("/v1/api/order/confirm")
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new AdminOrderConfirmRequest(
                                    List.of(order1.getId(), order2.getId())))))
                    .andExpect(status().isOk());

            em.flush();
            em.clear();

            Order updated1 = orderRepository.findById(order1.getId()).orElseThrow();
            Order updated2 = orderRepository.findById(order2.getId()).orElseThrow();
            assertThat(updated1.getOrderStatus()).isEqualTo(OrderStatus.ORDERED);
            assertThat(updated2.getOrderStatus()).isEqualTo(OrderStatus.ORDERED);

            List<Delivery> deliveries = deliveryRepository.findByOrderIn(List.of(updated1, updated2));
            assertThat(deliveries).hasSize(2);
            assertThat(deliveries).allMatch(d -> d.getDeliveryStatus() == DeliveryStatus.READY);
        }

        @Test
        void 존재하지_않는_주문ID_포함시_404() throws Exception {
            Order order = createOrder("GARAMCONF003", OrderStatus.PAYMENT_CONFIRMED, 50000);
            em.flush();
            em.clear();

            mockMvc.perform(post("/v1/api/order/confirm")
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new AdminOrderConfirmRequest(
                                    List.of(order.getId(), 999999L)))))
                    .andExpect(status().isNotFound());
        }

        @Test
        void 삭제된_주문ID_포함시_404() throws Exception {
            Order active = createOrder("GARAMCONF004", OrderStatus.PAYMENT_CONFIRMED, 50000);
            Order deleted = orderRepository.save(Order.builder()
                    .orderId("GARAMCONF005")
                    .user(savedUser).totalPrice(50000)
                    .orderStatus(OrderStatus.PAYMENT_CONFIRMED)
                    .orderAddress(new OrderAddress("수신자", "010-1234-5678", "12345", "서울시 강남구", "101호", ""))
                    .isDeleted(true)
                    .build());
            em.flush();
            em.clear();

            mockMvc.perform(post("/v1/api/order/confirm")
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new AdminOrderConfirmRequest(
                                    List.of(active.getId(), deleted.getId())))))
                    .andExpect(status().isNotFound());
        }

        @Test
        void 비인증_요청시_401() throws Exception {
            mockMvc.perform(post("/v1/api/order/confirm")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new AdminOrderConfirmRequest(List.of(1L)))))
                    .andExpect(status().isUnauthorized());
        }
    }
}
