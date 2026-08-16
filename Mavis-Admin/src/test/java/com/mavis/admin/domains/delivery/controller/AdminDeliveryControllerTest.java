package com.mavis.admin.domains.delivery.controller;

import com.mavis.admin.domains.delivery.dto.AdminCompleteDeliveryRequest;
import com.mavis.admin.domains.order.dto.AdminDeliveryStartRequest;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminDeliveryControllerTest extends ControllerTestSupport {

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

    private Order createOrder(String orderId, OrderStatus status) {
        return orderRepository.save(Order.builder()
                .orderId(orderId)
                .user(savedUser)
                .totalPrice(50000)
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
                .totalPrice(savedProduct.getPrice())
                .build());
    }

    private Delivery createShippedDelivery(Order order) {
        Delivery delivery = Delivery.builder()
                .order(order)
                .deliveryStatus(DeliveryStatus.READY)
                .build();
        delivery.startDelivery("CJ대한통운", "123456789");
        return deliveryRepository.save(delivery);
    }

    @Nested
    class 배송_목록_조회 {

        @Test
        void SHIPPED_배송_조회_JSON_필드_전체_검증() throws Exception {
            Order order = createOrder("GARAM20240301ABCD", OrderStatus.ORDERED);
            createOrderItem(order, "블랙", 2);
            Delivery delivery = createShippedDelivery(order);
            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/delivery")
                            .param("deliveryStatus", "SHIPPED")
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.content[0].deliveryId").value(delivery.getId()))
                    .andExpect(jsonPath("$.data.content[0].orderId").value("20240301ABCD"))
                    .andExpect(jsonPath("$.data.content[0].carrier").value("CJ대한통운"))
                    .andExpect(jsonPath("$.data.content[0].trackingNumber").value("123456789"))
                    .andExpect(jsonPath("$.data.content[0].receiverName").value("수신자"))
                    .andExpect(jsonPath("$.data.content[0].receiverPhoneNumber").value("010-1234-5678"))
                    .andExpect(jsonPath("$.data.content[0].address").value("서울시 강남구"))
                    .andExpect(jsonPath("$.data.content[0].requestMessage").value("문앞에 놔주세요"))
                    .andExpect(jsonPath("$.data.content[0].buyerName").value("홍길동"))
                    .andExpect(jsonPath("$.data.content[0].totalPrice").value(50000))
                    .andExpect(jsonPath("$.data.content[0].deliveryStatus").value("배송중"))
                    .andExpect(jsonPath("$.data.content[0].orderedAt").isNotEmpty())
                    .andExpect(jsonPath("$.data.content[0].orderItemInfos").isArray())
                    .andExpect(jsonPath("$.data.content[0].orderItemInfos[0].productName").value("테스트상품"))
                    .andExpect(jsonPath("$.data.content[0].orderItemInfos[0].color").value("블랙"))
                    .andExpect(jsonPath("$.data.content[0].orderItemInfos[0].quantity").value(2));
        }

        @Test
        void DELIVERED_배송_조회_성공() throws Exception {
            Order order = createOrder("GARAM20240301EFGH", OrderStatus.ORDERED);
            Delivery delivery = deliveryRepository.save(Delivery.builder()
                    .order(order)
                    .deliveryStatus(DeliveryStatus.DELIVERED)
                    .build());
            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/delivery")
                            .param("deliveryStatus", "DELIVERED")
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.content[0].deliveryId").value(delivery.getId()))
                    .andExpect(jsonPath("$.data.content[0].deliveryStatus").value("배송 완료"));
        }

        @Test
        void SHIPPED_조회시_DELIVERED_배송_미포함() throws Exception {
            Order order1 = createOrder("GARAM20240301IIII", OrderStatus.ORDERED);
            Order order2 = createOrder("GARAM20240301JJJJ", OrderStatus.ORDERED);
            createShippedDelivery(order1);
            deliveryRepository.save(Delivery.builder().order(order2).deliveryStatus(DeliveryStatus.DELIVERED).build());
            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/delivery")
                            .param("deliveryStatus", "SHIPPED")
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        void 배송_없을때_빈_목록_반환() throws Exception {
            mockMvc.perform(get("/v1/api/delivery")
                            .param("deliveryStatus", "SHIPPED")
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(0))
                    .andExpect(jsonPath("$.data.content").isEmpty());
        }

        @Test
        void 비인증_요청시_401() throws Exception {
            mockMvc.perform(get("/v1/api/delivery").param("deliveryStatus", "SHIPPED"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class 배송_시작 {

        @Test
        void 배송_시작_성공() throws Exception {
            Order order = createOrder("GARAM20240301CCCC", OrderStatus.ORDERED);
            deliveryRepository.save(Delivery.builder().order(order).deliveryStatus(DeliveryStatus.READY).build());
            em.flush();
            em.clear();

            mockMvc.perform(post("/v1/api/delivery/confirm/{orderId}", order.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new AdminDeliveryStartRequest("CJ대한통운", "987654321")))
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk());
        }

        @Test
        void ORDERED_아닌_주문이면_400() throws Exception {
            Order order = createOrder("GARAM20240301DDDD", OrderStatus.PAYMENT_CONFIRMED);
            deliveryRepository.save(Delivery.builder().order(order).deliveryStatus(DeliveryStatus.READY).build());
            em.flush();
            em.clear();

            mockMvc.perform(post("/v1/api/delivery/confirm/{orderId}", order.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new AdminDeliveryStartRequest("CJ대한통운", "987654321")))
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void 존재하지_않는_주문이면_404() throws Exception {
            mockMvc.perform(post("/v1/api/delivery/confirm/{orderId}", 999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new AdminDeliveryStartRequest("CJ대한통운", "987654321")))
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isNotFound());
        }

        @Test
        void 비인증_요청시_401() throws Exception {
            mockMvc.perform(post("/v1/api/delivery/confirm/{orderId}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new AdminDeliveryStartRequest("CJ대한통운", "987654321"))))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class 배송_완료 {

        @Test
        void 배송_완료_처리_성공() throws Exception {
            Order order1 = createOrder("GARAM20240301EEEE", OrderStatus.ORDERED);
            Order order2 = createOrder("GARAM20240301FFFF", OrderStatus.ORDERED);
            Delivery delivery1 = createShippedDelivery(order1);
            Delivery delivery2 = createShippedDelivery(order2);
            em.flush();
            em.clear();

            mockMvc.perform(patch("/v1/api/delivery/complete")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new AdminCompleteDeliveryRequest(List.of(delivery1.getId(), delivery2.getId()))))
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isOk());
        }

        @Test
        void 존재하지_않는_배송ID_포함시_404() throws Exception {
            Order order = createOrder("GARAM20240301GGGG", OrderStatus.ORDERED);
            Delivery delivery = createShippedDelivery(order);
            em.flush();
            em.clear();

            mockMvc.perform(patch("/v1/api/delivery/complete")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new AdminCompleteDeliveryRequest(List.of(delivery.getId(), 999L))))
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isNotFound());
        }

        @Test
        void SHIPPED_아닌_배송_완료_처리시_400() throws Exception {
            Order order = createOrder("GARAM20240301HHHH", OrderStatus.ORDERED);
            Delivery delivery = deliveryRepository.save(Delivery.builder()
                    .order(order)
                    .deliveryStatus(DeliveryStatus.READY)
                    .build());
            em.flush();
            em.clear();

            mockMvc.perform(patch("/v1/api/delivery/complete")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new AdminCompleteDeliveryRequest(List.of(delivery.getId()))))
                            .with(user(savedAdmin.getId().toString()).roles("ADMIN")))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void 비인증_요청시_401() throws Exception {
            mockMvc.perform(patch("/v1/api/delivery/complete")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new AdminCompleteDeliveryRequest(List.of(1L)))))
                    .andExpect(status().isUnauthorized());
        }
    }
}
