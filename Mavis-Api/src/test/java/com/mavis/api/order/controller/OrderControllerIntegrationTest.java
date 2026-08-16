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
import com.mavis.domain.domains.product.domain.ProductImage;
import com.mavis.domain.domains.product.domain.ProductImageType;
import com.mavis.domain.domains.product.repository.ProductImageRepository;
import com.mavis.domain.domains.product.repository.ProductRepository;
import com.mavis.domain.domains.refund.domain.Refund;
import com.mavis.domain.domains.refund.domain.RefundStatus;
import com.mavis.domain.domains.refund.domain.RefundType;
import com.mavis.domain.domains.refund.repository.RefundRepository;
import com.mavis.domain.domains.user.domain.SnsType;
import com.mavis.domain.domains.user.domain.User;
import com.mavis.domain.domains.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.List;

import static org.hamcrest.Matchers.notNullValue;
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
        @Autowired private ProductImageRepository productImageRepository;
        @Autowired private RefundRepository refundRepository;
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
            orderItemRepository.save(OrderItem.of(new OrderOption("black", 2), 20000, orderNoDelivery, product));

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
                    // 취소 주문 → 주문 레벨 환불금액(배송비 포함 총액) 노출
                    .andExpect(jsonPath("$.data.content[0].refundAmount").value(10000))
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
                    .andExpect(jsonPath("$.data.content[0].orderProductList[0].refundAmount", nullValue()))
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
                    .andExpect(jsonPath("$.data.content[3].refundAmount", nullValue()))
                    .andExpect(jsonPath("$.data.content[3].paymentMethod").value("CARD"))
                    .andExpect(jsonPath("$.data.content[3].orderProductList[0].option.color").value("black"))
                    .andExpect(jsonPath("$.data.content[3].orderProductList[0].option.quantity").value(2))
                    .andExpect(jsonPath("$.data.content[3].orderProductList[0].totalPrice").value(20000));
        }

        @Test
        void 상품_2개_주문_조회시_상품별_그룹핑되고_환불상태와_MAIN이미지_매핑된다() throws Exception {
            Product imageProduct = productRepository.save(Product.builder()
                    .name("이미지상품")
                    .price(20000)
                    .subCategory(ProductSubCategory.TENCEL)
                    .build());
            productImageRepository.save(ProductImage.builder()
                    .product(imageProduct)
                    .imageType(ProductImageType.MAIN)
                    .imageUrl("https://cdn.test/main.jpg")
                    .orderNum(0)
                    .build());

            OrderAddress address = new OrderAddress("홍길동", "010-1234-5678", "12345", "서울시 강남구", "101호", "문 앞에 놔주세요");
            Order order = orderRepository.save(Order.builder()
                    .orderId("GARAM999")
                    .user(user)
                    .totalPrice(30000)
                    .orderAddress(address)
                    .paymentMethod(PaymentMethod.CARD)
                    .orderStatus(OrderStatus.PAYMENT_CONFIRMED)
                    .build());
            orderItemRepository.save(OrderItem.of(new OrderOption("black", 1), 10000, order, product));
            OrderItem refundedItem = orderItemRepository.save(OrderItem.of(new OrderOption("white", 1), 20000, order, imageProduct));
            refundRepository.save(Refund.builder()
                    .orderItem(refundedItem)
                    .refundType(RefundType.RETURN)
                    .refundStatus(RefundStatus.REQUESTED)
                    .refundAmount(20000)
                    .refundReason("단순 변심")
                    .carrier("CJ대한통운")
                    .trackingNumber("1234567890")
                    .build());

            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/order")
                            .header("Authorization", userToken(user.getId())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    // 반품(RETURN)은 주문 취소가 아니므로 주문 레벨 환불금액 없음
                    .andExpect(jsonPath("$.data.content[0].refundAmount", nullValue()))
                    // 그룹핑: 한 주문에 상품 2개
                    .andExpect(jsonPath("$.data.content[0].orderProductList.length()").value(2))
                    // item1 (id asc 먼저) = 이미지/환불 없음
                    .andExpect(jsonPath("$.data.content[0].orderProductList[0].productName").value("테스트상품"))
                    .andExpect(jsonPath("$.data.content[0].orderProductList[0].option.color").value("black"))
                    .andExpect(jsonPath("$.data.content[0].orderProductList[0].productImageUrl", nullValue()))
                    .andExpect(jsonPath("$.data.content[0].orderProductList[0].refundStatus", nullValue()))
                    .andExpect(jsonPath("$.data.content[0].orderProductList[0].refundStatusTitle", nullValue()))
                    .andExpect(jsonPath("$.data.content[0].orderProductList[0].refundAmount", nullValue()))
                    // item2 = MAIN 이미지 + 환불 채워짐, 반품이므로 상품 레벨 환불금액(배송비 제외 상품값) 노출
                    .andExpect(jsonPath("$.data.content[0].orderProductList[1].productName").value("이미지상품"))
                    .andExpect(jsonPath("$.data.content[0].orderProductList[1].option.color").value("white"))
                    .andExpect(jsonPath("$.data.content[0].orderProductList[1].productImageUrl").value("https://cdn.test/main.jpg"))
                    .andExpect(jsonPath("$.data.content[0].orderProductList[1].refundStatus").value("REQUESTED"))
                    .andExpect(jsonPath("$.data.content[0].orderProductList[1].refundStatusTitle", notNullValue()))
                    .andExpect(jsonPath("$.data.content[0].orderProductList[1].refundAmount").value(20000));
        }

        @Test
        void 삭제된_주문상품은_목록에서_제외된다() throws Exception {
            OrderAddress address = new OrderAddress("홍길동", "010-1234-5678", "12345", "서울시 강남구", "101호", "문 앞에 놔주세요");
            Order order = orderRepository.save(Order.builder()
                    .orderId("GARAM888")
                    .user(user)
                    .totalPrice(10000)
                    .orderAddress(address)
                    .paymentMethod(PaymentMethod.CARD)
                    .orderStatus(OrderStatus.PAYMENT_CONFIRMED)
                    .build());
            orderItemRepository.save(OrderItem.of(new OrderOption("black", 1), 10000, order, product));
            orderItemRepository.save(OrderItem.builder()
                    .order(order)
                    .product(product)
                    .color("white")
                    .quantity(1)
                    .totalPrice(10000)
                    .isDeleted(true)
                    .build());

            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/order")
                            .header("Authorization", userToken(user.getId())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    // 삭제 안 된 1개만 노출
                    .andExpect(jsonPath("$.data.content[0].orderProductList.length()").value(1))
                    .andExpect(jsonPath("$.data.content[0].orderProductList[0].option.color").value("black"));
        }

        @Test
        void 주문취소시_환불금액은_주문레벨_배송비포함_상품레벨은_노출안된다() throws Exception {
            OrderAddress address = new OrderAddress("홍길동", "010-1234-5678", "12345", "서울시 강남구", "101호", "문 앞에 놔주세요");
            // 상품 20000 + 배송비 4000 = 총 24000, 전체 취소
            Order order = orderRepository.save(Order.builder()
                    .orderId("GARAM777")
                    .user(user)
                    .totalPrice(24000)
                    .deliveryFee(4000)
                    .orderAddress(address)
                    .orderStatus(OrderStatus.CANCELED)
                    .build());
            OrderItem orderItem = orderItemRepository.save(OrderItem.of(new OrderOption("black", 2), 20000, order, product));
            // 주문 취소는 상품별 CANCEL Refund 생성 (RETURN 아님)
            refundRepository.save(Refund.builder()
                    .orderItem(orderItem)
                    .refundType(RefundType.CANCEL)
                    .refundStatus(RefundStatus.COMPLETED)
                    .refundAmount(20000)
                    .refundReason("주문 취소")
                    .build());

            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/order")
                            .header("Authorization", userToken(user.getId())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    // 주문 레벨: 배송비 포함 총액
                    .andExpect(jsonPath("$.data.content[0].totalPrice").value(24000))
                    .andExpect(jsonPath("$.data.content[0].refundAmount").value(24000))
                    // 상품 레벨: CANCEL 유형은 상품 환불금액 미노출(null)
                    .andExpect(jsonPath("$.data.content[0].orderProductList[0].refundStatus").value("COMPLETED"))
                    .andExpect(jsonPath("$.data.content[0].orderProductList[0].refundAmount", nullValue()));
        }
    }

}
