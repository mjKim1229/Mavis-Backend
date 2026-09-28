package com.mavis.api.refund.controller;

import com.mavis.api.refund.dto.RequestReturnRequest;
import com.mavis.api.support.ControllerTestSupport;
import com.mavis.common.enums.ProductSubCategory;
import com.mavis.domain.domains.delivery.domain.Delivery;
import com.mavis.domain.domains.delivery.domain.DeliveryStatus;
import com.mavis.domain.domains.delivery.repository.DeliveryRepository;
import com.mavis.domain.domains.order.domain.*;
import com.mavis.domain.domains.order.repository.OrderItemRepository;
import com.mavis.domain.domains.order.repository.OrderRepository;
import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.product.domain.ProductColor;
import com.mavis.domain.domains.product.repository.ProductColorRepository;
import com.mavis.domain.domains.product.repository.ProductRepository;
import com.mavis.domain.domains.claim.domain.Claim;
import com.mavis.domain.domains.claim.domain.ClaimReturn;
import com.mavis.domain.domains.claim.domain.ClaimStatus;
import com.mavis.domain.domains.claim.domain.ClaimType;
import com.mavis.domain.domains.claim.domain.FaultParty;
import com.mavis.domain.domains.claim.repository.ClaimRepository;
import com.mavis.domain.domains.claim.repository.ClaimReturnRepository;
import com.mavis.domain.domains.user.domain.SnsType;
import com.mavis.domain.domains.user.domain.User;
import com.mavis.domain.domains.user.repository.UserRepository;
import com.mavis.infrastructure.image.S3FileUploader;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RefundControllerTest extends ControllerTestSupport {

    @MockitoBean
    S3FileUploader s3FileUploader;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductColorRepository productColorRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private DeliveryRepository deliveryRepository;
    @Autowired
    private ClaimRepository claimRepository;
    @Autowired
    private ClaimReturnRepository claimReturnRepository;
    @Autowired
    private EntityManager em;

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

        productColorRepository.save(ProductColor.of(product, "black"));
        productColorRepository.save(ProductColor.of(product, "white"));
        // Product.colors는 이미 초기화된 빈 리스트라 색상 저장 후 재조회가 필요하다
        em.flush();
        em.clear();
        product = productRepository.findById(product.getId()).orElseThrow();
        user = userRepository.findById(user.getId()).orElseThrow();

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
        given(s3FileUploader.uploadImageToS3(any(), any())).willReturn("https://cdn.test/return.jpg");

        em.flush();
        em.clear();

        mockMvc.perform(multipart("/v1/api/refund/{orderItemId}/return", item.getId())
                .file(requestPart("변심"))
                .file(imagePart())
                .header("Authorization", userToken(user.getId())))
            .andExpect(status().isOk());

        em.flush();
        em.clear();

        List<Claim> claims = claimRepository.findAll();
        assertThat(claims).hasSize(1);
        Claim claim = claims.get(0);
        assertThat(claim.getClaimType()).isEqualTo(ClaimType.RETURN);
        assertThat(claim.getClaimStatus()).isEqualTo(ClaimStatus.REQUESTED);
        assertThat(claim.getFaultParty()).isNull();
        assertThat(claim.getReason()).isEqualTo("변심");
        assertThat(claim.getItems()).extracting(claimItem -> claimItem.getOrderItem().getId()).containsExactly(item.getId());
        assertThat(claim.getImages()).extracting("imageUrl").containsExactly("https://cdn.test/return.jpg");

        List<ClaimReturn> claimReturns = claimReturnRepository.findByClaimIn(claims);
        assertThat(claimReturns).hasSize(1);
        assertThat(claimReturns.get(0).getCarrier()).isEqualTo("CJ대한통운");
        assertThat(claimReturns.get(0).getTrackingNumber()).isEqualTo("12345678");
    }

    @Test
    void 택배사_없이_반품_신청시_400() throws Exception {
        Order order = savedDeliveredOrder("GARAM007");
        OrderItem item = orderItemRepository.save(OrderItem.of(new OrderOption("black", 1), 10000, order, product));
        MockMultipartFile noCarrier = new MockMultipartFile("request", "", MediaType.APPLICATION_JSON_VALUE,
            objectMapper.writeValueAsBytes(new RequestReturnRequest("변심", " ", "12345678")));

        em.flush();
        em.clear();

        mockMvc.perform(multipart("/v1/api/refund/{orderItemId}/return", item.getId())
                .file(noCarrier)
                .file(imagePart())
                .header("Authorization", userToken(user.getId())))
            .andExpect(status().isBadRequest());

        assertThat(claimRepository.count()).isZero();
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
        claimRepository.save(Claim.requestReturn(item, "변심"));

        em.flush();
        em.clear();

        mockMvc.perform(multipart("/v1/api/refund/{orderItemId}/return", item.getId())
                .file(requestPart("변심"))
                .file(imagePart())
                .header("Authorization", userToken(user.getId())))
            .andExpect(status().isConflict());
    }

    @Test
    void 환불_완료된_주문_재신청시_409() throws Exception {
        Order order = savedDeliveredOrder("GARAM005");
        OrderItem item = orderItemRepository.save(OrderItem.of(new OrderOption("black", 1), 10000, order, product));
        Claim completed = Claim.requestReturn(item, "변심");
        completed.complete(FaultParty.BUYER);
        claimRepository.save(completed);

        em.flush();
        em.clear();

        mockMvc.perform(multipart("/v1/api/refund/{orderItemId}/return", item.getId())
                .file(requestPart("변심"))
                .file(imagePart())
                .header("Authorization", userToken(user.getId())))
            .andExpect(status().isConflict());
    }

    @Test
    void 환불_거절된_주문_재신청시_409() throws Exception {
        Order order = savedDeliveredOrder("GARAM006");
        OrderItem item = orderItemRepository.save(OrderItem.of(new OrderOption("black", 1), 10000, order, product));
        Claim rejected = Claim.requestReturn(item, "변심");
        rejected.reject();
        claimRepository.save(rejected);

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
