package com.mavis.api.order.controller;

import com.mavis.api.order.dto.CreateOrderRequest;
import com.mavis.api.order.dto.OrderAddressRequest;
import com.mavis.api.order.dto.OrderItemRequest;
import com.mavis.api.support.ControllerTestSupport;
import com.mavis.common.enums.ProductSubCategory;
import com.mavis.domain.domains.delivery.domain.Delivery;
import com.mavis.domain.domains.delivery.domain.DeliveryStatus;
import com.mavis.domain.domains.delivery.repository.DeliveryRepository;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderAddress;
import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.order.domain.OrderOption;
import com.mavis.domain.domains.order.domain.OrderStatus;
import com.mavis.domain.domains.order.domain.PaymentMethod;
import com.mavis.domain.domains.order.repository.OrderItemRepository;
import com.mavis.domain.domains.order.repository.OrderRepository;
import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.product.repository.ProductRepository;
import com.mavis.domain.domains.review.domain.Review;
import com.mavis.domain.domains.review.repository.ReviewRepository;
import com.mavis.domain.domains.user.domain.SnsType;
import com.mavis.domain.domains.user.domain.User;
import com.mavis.domain.domains.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.List;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderControllerIntegrationTest extends ControllerTestSupport {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void 주문_생성_성공() throws Exception {
        User user = userRepository.save(User.builder()
                .snsType(SnsType.KAKAO)
                .name("테스트유저")
                .build());

        Product product = productRepository.save(Product.builder()
                .name("테스트상품")
                .price(10000)
                .subCategory(ProductSubCategory.TENCEL)
                .build());

        CreateOrderRequest request = new CreateOrderRequest(
                14000,
                new OrderAddressRequest("홍길동", "010-1234-5678", "12345", "서울시 강남구", "101호", "문 앞에 놔주세요"),
                List.of(new OrderItemRequest(product.getId(), new OrderOption("black", 1)))
        );

        mockMvc.perform(post("/v1/api/order")
                        .header("Authorization", userToken(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tossOrderId").exists());
    }

    @Test
    void 주문_금액_불일치시_400_반환() throws Exception {
        User user = userRepository.save(User.builder()
                .snsType(SnsType.KAKAO)
                .name("테스트유저")
                .build());

        Product product = productRepository.save(Product.builder()
                .name("테스트상품")
                .price(10000)
                .subCategory(ProductSubCategory.TENCEL)
                .build());

        CreateOrderRequest request = new CreateOrderRequest(
                99999, // 틀린 금액
                new OrderAddressRequest("홍길동", "010-1234-5678", "12345", "서울시 강남구", "101호", "문 앞에 놔주세요"),
                List.of(new OrderItemRequest(product.getId(), new OrderOption("black", 1)))
        );

        mockMvc.perform(post("/v1/api/order")
                        .header("Authorization", userToken(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 인증_없이_주문_생성시_401_반환() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest(
                14000,
                new OrderAddressRequest("홍길동", "010-1234-5678", "12345", "서울시 강남구", "101호", "문 앞에 놔주세요"),
                List.of()
        );

        mockMvc.perform(post("/v1/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Nested
    class 주문목록조회 {

        @Autowired private OrderRepository orderRepository;
        @Autowired private OrderItemRepository orderItemRepository;
        @Autowired private DeliveryRepository deliveryRepository;
        @Autowired private jakarta.persistence.EntityManager em;

        private User user;
        private Product product;

        @BeforeEach
        void setUp() {
            user = userRepository.save(User.builder()
                    .snsType(SnsType.KAKAO)
                    .name("테스트유저")
                    .build());

            product = productRepository.save(Product.builder()
                    .name("테스트상품")
                    .price(10000)
                    .subCategory(ProductSubCategory.TENCEL)
                    .build());
        }

        @Test
        void 결제완료_배송준비_배송중_취소_4가지_주문_전_필드_검증() throws Exception {
            OrderAddress address = new OrderAddress("홍길동", "010-1234-5678", "12345", "서울시 강남구", "101호", "문 앞에 놔주세요");

            // Order1: PAYMENT_CONFIRMED, 배송 없음 → orderStatus 그대로 노출
            Order orderNoDelivery = orderRepository.save(Order.builder()
                    .orderId("GARAM111")
                    .user(user)
                    .totalPrice(20000)
                    .orderAddress(address)
                    .paymentMethod(PaymentMethod.CARD)
                    .orderStatus(OrderStatus.PAYMENT_CONFIRMED)
                    .build());
            orderItemRepository.save(OrderItem.of(new OrderOption("black", 2), 10000, orderNoDelivery, product));

            // Order2: ORDERED + delivery READY → deliveryStatus READY 노출
            Order orderDeliveryReady = orderRepository.save(Order.builder()
                    .orderId("GARAM222")
                    .user(user)
                    .totalPrice(10000)
                    .orderAddress(address)
                    .paymentMethod(PaymentMethod.CARD)
                    .orderStatus(OrderStatus.ORDERED)
                    .build());
            orderItemRepository.save(OrderItem.of(new OrderOption("white", 1), 10000, orderDeliveryReady, product));
            deliveryRepository.save(Delivery.builder().order(orderDeliveryReady).build());

            // Order3: ORDERED + delivery SHIPPED → deliveryStatus SHIPPED 노출
            Order orderDeliveryShipped = orderRepository.save(Order.builder()
                    .orderId("GARAM333")
                    .user(user)
                    .totalPrice(10000)
                    .orderAddress(address)
                    .paymentMethod(PaymentMethod.CARD)
                    .orderStatus(OrderStatus.ORDERED)
                    .build());
            orderItemRepository.save(OrderItem.of(new OrderOption("black", 1), 10000, orderDeliveryShipped, product));
            deliveryRepository.save(Delivery.builder().order(orderDeliveryShipped).deliveryStatus(DeliveryStatus.SHIPPED).build());

            // Order4: CANCELED → 배송 유무 무관하게 CANCELED 노출
            Order orderCanceled = orderRepository.save(Order.builder()
                    .orderId("GARAM444")
                    .user(user)
                    .totalPrice(10000)
                    .orderAddress(address)
                    .orderStatus(OrderStatus.CANCELED)
                    .build());
            orderItemRepository.save(OrderItem.of(new OrderOption("black", 1), 10000, orderCanceled, product));

            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/order")
                            .header("Authorization", userToken(user.getId())))
                    .andExpect(status().isOk())
                    // 페이지 메타
                    .andExpect(jsonPath("$.data.totalElements").value(4))
                    .andExpect(jsonPath("$.data.page").value(0))
                    .andExpect(jsonPath("$.data.hasNext").value(false))
                    // content[0] = Order4 (id desc) — CANCELED, paymentMethod null
                    .andExpect(jsonPath("$.data.content[0].tossOrderId").value("444"))
                    .andExpect(jsonPath("$.data.content[0].orderStatusCode").value("CANCELED"))
                    .andExpect(jsonPath("$.data.content[0].orderStatus").value("주문 취소"))
                    .andExpect(jsonPath("$.data.content[0].totalPrice").value(10000))
                    .andExpect(jsonPath("$.data.content[0].userName").value("테스트유저"))
                    .andExpect(jsonPath("$.data.content[0].address").value("서울시 강남구"))
                    .andExpect(jsonPath("$.data.content[0].addressInfo").value("101호"))
                    .andExpect(jsonPath("$.data.content[0].paymentMethod", nullValue()))
                    .andExpect(jsonPath("$.data.content[0].createdAt").exists())
                    .andExpect(jsonPath("$.data.content[0].orderId").exists())
                    .andExpect(jsonPath("$.data.content[0].orderProductList").isArray())
                    .andExpect(jsonPath("$.data.content[0].orderProductList[0].orderItemId").exists())
                    .andExpect(jsonPath("$.data.content[0].orderProductList[0].productId").exists())
                    .andExpect(jsonPath("$.data.content[0].orderProductList[0].productName").value("테스트상품"))
                    .andExpect(jsonPath("$.data.content[0].orderProductList[0].option.color").value("black"))
                    .andExpect(jsonPath("$.data.content[0].orderProductList[0].option.quantity").value(1))
                    .andExpect(jsonPath("$.data.content[0].orderProductList[0].totalPrice").value(10000))
                    .andExpect(jsonPath("$.data.content[0].orderProductList[0].refundStatus", nullValue()))
                    .andExpect(jsonPath("$.data.content[0].orderProductList[0].refundStatusTitle", nullValue()))
                    .andExpect(jsonPath("$.data.content[0].orderProductList[0].productImageUrl", nullValue()))
                    // content[1] = Order3 — SHIPPED
                    .andExpect(jsonPath("$.data.content[1].tossOrderId").value("333"))
                    .andExpect(jsonPath("$.data.content[1].orderStatusCode").value("SHIPPED"))
                    .andExpect(jsonPath("$.data.content[1].orderStatus").value("배송중"))
                    .andExpect(jsonPath("$.data.content[1].totalPrice").value(10000))
                    .andExpect(jsonPath("$.data.content[1].paymentMethod").value("CARD"))
                    .andExpect(jsonPath("$.data.content[1].orderProductList[0].option.color").value("black"))
                    .andExpect(jsonPath("$.data.content[1].orderProductList[0].option.quantity").value(1))
                    .andExpect(jsonPath("$.data.content[1].orderProductList[0].totalPrice").value(10000))
                    // content[2] = Order2 — delivery READY
                    .andExpect(jsonPath("$.data.content[2].tossOrderId").value("222"))
                    .andExpect(jsonPath("$.data.content[2].orderStatusCode").value("READY"))
                    .andExpect(jsonPath("$.data.content[2].orderStatus").value("배송 준비"))
                    .andExpect(jsonPath("$.data.content[2].paymentMethod").value("CARD"))
                    .andExpect(jsonPath("$.data.content[2].orderProductList[0].option.color").value("white"))
                    .andExpect(jsonPath("$.data.content[2].orderProductList[0].option.quantity").value(1))
                    // content[3] = Order1 — PAYMENT_CONFIRMED, 배송 없음
                    .andExpect(jsonPath("$.data.content[3].tossOrderId").value("111"))
                    .andExpect(jsonPath("$.data.content[3].orderStatusCode").value("PAYMENT_CONFIRMED"))
                    .andExpect(jsonPath("$.data.content[3].orderStatus").value("결제 완료"))
                    .andExpect(jsonPath("$.data.content[3].totalPrice").value(20000))
                    .andExpect(jsonPath("$.data.content[3].paymentMethod").value("CARD"))
                    .andExpect(jsonPath("$.data.content[3].orderProductList[0].option.color").value("black"))
                    .andExpect(jsonPath("$.data.content[3].orderProductList[0].option.quantity").value(2))
                    .andExpect(jsonPath("$.data.content[3].orderProductList[0].totalPrice").value(20000));
        }
    }

    @Nested
    class 리뷰가능목록조회 {

        @Autowired private OrderRepository orderRepository;
        @Autowired private OrderItemRepository orderItemRepository;
        @Autowired private DeliveryRepository deliveryRepository;
        @Autowired private ReviewRepository reviewRepository;
        @Autowired private jakarta.persistence.EntityManager em;

        private User user;
        private Product product;

        @BeforeEach
        void setUp() {
            user = userRepository.save(User.builder()
                    .snsType(SnsType.KAKAO)
                    .name("테스트유저")
                    .build());

            product = productRepository.save(Product.builder()
                    .name("테스트상품")
                    .price(10000)
                    .subCategory(ProductSubCategory.TENCEL)
                    .build());
        }

        @Test
        void 배송완료_리뷰미작성_아이템만_노출() throws Exception {
            OrderAddress address = new OrderAddress("홍길동", "010-1234-5678", "12345", "서울시 강남구", "101호", "문 앞에 놔주세요");

            // Case1: DELIVERED + 리뷰 없음 → 포함
            Order deliveredOrder = orderRepository.save(Order.builder()
                    .orderId("GARAM001")
                    .user(user).totalPrice(10000).orderAddress(address)
                    .orderStatus(OrderStatus.ORDERED).build());
            OrderItem reviewableItem = orderItemRepository.save(
                    OrderItem.of(new OrderOption("black", 1), 10000, deliveredOrder, product));
            deliveryRepository.save(Delivery.builder()
                    .order(deliveredOrder).deliveryStatus(DeliveryStatus.DELIVERED).build());

            // Case2: DELIVERED + 리뷰 있음 → 제외
            Order deliveredOrderWithReview = orderRepository.save(Order.builder()
                    .orderId("GARAM002")
                    .user(user).totalPrice(10000).orderAddress(address)
                    .orderStatus(OrderStatus.ORDERED).build());
            OrderItem reviewedItem = orderItemRepository.save(
                    OrderItem.of(new OrderOption("white", 1), 10000, deliveredOrderWithReview, product));
            deliveryRepository.save(Delivery.builder()
                    .order(deliveredOrderWithReview).deliveryStatus(DeliveryStatus.DELIVERED).build());
            reviewRepository.save(Review.builder().orderItem(reviewedItem).user(user).content("좋아요").build());

            // Case3: SHIPPED + 리뷰 없음 → 제외 (배송완료 아님)
            Order shippedOrder = orderRepository.save(Order.builder()
                    .orderId("GARAM003")
                    .user(user).totalPrice(10000).orderAddress(address)
                    .orderStatus(OrderStatus.ORDERED).build());
            orderItemRepository.save(OrderItem.of(new OrderOption("black", 1), 10000, shippedOrder, product));
            deliveryRepository.save(Delivery.builder()
                    .order(shippedOrder).deliveryStatus(DeliveryStatus.SHIPPED).build());

            // Case4: PAYMENT_CONFIRMED + 배송 없음 → 제외
            Order noDeliveryOrder = orderRepository.save(Order.builder()
                    .orderId("GARAM004")
                    .user(user).totalPrice(10000).orderAddress(address)
                    .orderStatus(OrderStatus.PAYMENT_CONFIRMED).build());
            orderItemRepository.save(OrderItem.of(new OrderOption("black", 1), 10000, noDeliveryOrder, product));

            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/order/reviewable")
                            .header("Authorization", userToken(user.getId())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.content[0].orderItemId").value(reviewableItem.getId()))
                    .andExpect(jsonPath("$.data.content[0].productName").value("테스트상품"))
                    .andExpect(jsonPath("$.data.content[0].orderOption.color").value("black"))
                    .andExpect(jsonPath("$.data.content[0].orderOption.quantity").value(1));
        }
    }
}
