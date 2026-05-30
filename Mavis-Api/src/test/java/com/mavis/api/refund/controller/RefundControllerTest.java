package com.mavis.api.refund.controller;

import com.mavis.api.refund.dto.RequestReturnRequest;
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
import com.mavis.domain.domains.order.repository.OrderItemRepository;
import com.mavis.domain.domains.order.repository.OrderRepository;
import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.product.repository.ProductRepository;
import com.mavis.domain.domains.refund.domain.Refund;
import com.mavis.domain.domains.refund.domain.RefundType;
import com.mavis.domain.domains.refund.repository.RefundRepository;
import com.mavis.domain.domains.user.domain.SnsType;
import com.mavis.domain.domains.user.domain.User;
import com.mavis.domain.domains.user.repository.UserRepository;
import com.mavis.infrastructure.image.S3FileUploader;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RefundControllerTest extends ControllerTestSupport {

    @MockitoBean
    S3FileUploader s3FileUploader;

    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private DeliveryRepository deliveryRepository;
    @Autowired private RefundRepository refundRepository;
    @Autowired private EntityManager em;

    private User user;
    private Product product;
    private OrderAddress address;

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

        address = new OrderAddress("홍길동", "010-1234-5678", "12345", "서울시 강남구", "101호", "문 앞에 놔주세요");
    }

    private Order savedDeliveredOrder(String orderId) {
        Order order = orderRepository.save(Order.builder()
                .orderId(orderId).user(user).totalPrice(10000)
                .orderAddress(address).orderStatus(OrderStatus.ORDERED).build());
        deliveryRepository.save(Delivery.builder()
                .order(order).deliveryStatus(DeliveryStatus.DELIVERED).build());
        return order;
    }

    private MockMultipartFile requestPart(String reason) throws Exception {
        return new MockMultipartFile("request", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(new RequestReturnRequest(reason, "CJ대한통운", "12345678")));
    }

    private MockMultipartFile imagePart() {
        return new MockMultipartFile("images", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "image".getBytes());
    }

    @Test
    void 반품_신청_성공() throws Exception {
        Order order = savedDeliveredOrder("GARAM001");
        OrderItem item = orderItemRepository.save(OrderItem.of(new OrderOption("black", 1), 10000, order, product));

        em.flush();
        em.clear();

        mockMvc.perform(multipart("/v1/api/refund/{orderItemId}/return", item.getId())
                        .file(requestPart("변심"))
                        .file(imagePart())
                        .header("Authorization", userToken(user.getId())))
                .andExpect(status().isOk());
    }

    @Test
    void 배송완료_아닌_주문_반품_신청시_400() throws Exception {
        Order order = orderRepository.save(Order.builder()
                .orderId("GARAM002").user(user).totalPrice(10000)
                .orderAddress(address).orderStatus(OrderStatus.ORDERED).build());
        deliveryRepository.save(Delivery.builder()
                .order(order).deliveryStatus(DeliveryStatus.SHIPPED).build());
        OrderItem item = orderItemRepository.save(OrderItem.of(new OrderOption("black", 1), 10000, order, product));

        em.flush();
        em.clear();

        mockMvc.perform(multipart("/v1/api/refund/{orderItemId}/return", item.getId())
                        .file(requestPart("변심"))
                        .file(imagePart())
                        .header("Authorization", userToken(user.getId())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 이미_환불_신청된_주문_재신청시_409() throws Exception {
        Order order = savedDeliveredOrder("GARAM003");
        OrderItem item = orderItemRepository.save(OrderItem.of(new OrderOption("black", 1), 10000, order, product));
        refundRepository.save(Refund.builder()
                .orderItem(item).refundType(RefundType.RETURN).refundAmount(10000).build());

        em.flush();
        em.clear();

        mockMvc.perform(multipart("/v1/api/refund/{orderItemId}/return", item.getId())
                        .file(requestPart("변심"))
                        .file(imagePart())
                        .header("Authorization", userToken(user.getId())))
                .andExpect(status().isConflict());
    }

    @Test
    void 다른_유저_주문_반품_신청시_403() throws Exception {
        User otherUser = userRepository.save(User.builder()
                .snsType(SnsType.KAKAO).name("다른유저").build());

        Order order = savedDeliveredOrder("GARAM004");
        OrderItem item = orderItemRepository.save(OrderItem.of(new OrderOption("black", 1), 10000, order, product));

        em.flush();
        em.clear();

        mockMvc.perform(multipart("/v1/api/refund/{orderItemId}/return", item.getId())
                        .file(requestPart("변심"))
                        .file(imagePart())
                        .header("Authorization", userToken(otherUser.getId())))
                .andExpect(status().isForbidden());
    }

    @Test
    void 비인증_반품_신청시_401() throws Exception {
        mockMvc.perform(multipart("/v1/api/refund/999/return")
                        .file(requestPart("변심"))
                        .file(imagePart()))
                .andExpect(status().isUnauthorized());
    }
}
