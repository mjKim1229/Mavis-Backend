package com.mavis.api.refund.controller;

import com.mavis.api.refund.dto.RequestReturnRequest;
import com.mavis.api.refund.dto.RequestReturnRequest.ReturnItemRequest;
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
import com.mavis.domain.domains.claim.domain.ClaimItem;
import com.mavis.domain.domains.claim.domain.ClaimReasonType;
import com.mavis.domain.domains.claim.domain.ReturnItemReason;
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

    private MockMultipartFile requestPart(String reason, Long... orderItemIds) throws Exception {
        return requestPart(ClaimReasonType.CHANGE_OF_MIND, reason, orderItemIds);
    }

    private MockMultipartFile requestPart(ClaimReasonType reasonType, String reason, Long... orderItemIds) throws Exception {
        return new MockMultipartFile("request", "", MediaType.APPLICATION_JSON_VALUE,
            objectMapper.writeValueAsBytes(new RequestReturnRequest(reasonType, Arrays.stream(orderItemIds).map(id -> new ReturnItemRequest(id, reason)).toList())));
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

        mockMvc.perform(multipart("/v1/api/refund/return")
                .file(requestPart("변심", item.getId()))
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
        assertThat(claim.getReasonType()).isEqualTo(ClaimReasonType.CHANGE_OF_MIND);
        assertThat(claim.getReason()).isEqualTo("변심");
        assertThat(claim.getItems()).extracting(claimItem -> claimItem.getOrderItem().getId()).containsExactly(item.getId());
        assertThat(claim.getImages()).extracting("imageUrl").containsExactly("https://cdn.test/return.jpg");

        assertThat(claimReturnRepository.count()).isZero();
    }

    @Test
    void 한_주문의_여러_상품을_묶어_반품_신청하면_클레임_하나에_담긴다() throws Exception {
        Order order = savedDeliveredOrder("GARAM008");
        OrderItem item1 = orderItemRepository.save(OrderItem.of(new OrderOption("black", 1), 10000, order, product));
        OrderItem item2 = orderItemRepository.save(OrderItem.of(new OrderOption("white", 1), 10000, order, product));
        given(s3FileUploader.uploadImageToS3(any(), any())).willReturn("https://cdn.test/return.jpg");

        em.flush();
        em.clear();

        mockMvc.perform(multipart("/v1/api/refund/return")
                .file(requestPart(ClaimReasonType.SELLER_FAULT, "불량", item1.getId(), item2.getId()))
                .file(imagePart())
                .header("Authorization", userToken(user.getId())))
            .andExpect(status().isOk());

        em.flush();
        em.clear();

        List<Claim> claims = claimRepository.findAll();
        assertThat(claims).hasSize(1);
        Claim claim = claims.get(0);
        assertThat(claim.getReasonType()).isEqualTo(ClaimReasonType.SELLER_FAULT);
        assertThat(claim.getItems()).extracting(claimItem -> claimItem.getOrderItem().getId())
            .containsExactlyInAnyOrder(item1.getId(), item2.getId());
    }

    @Test
    void 묶음_반품은_상품별_사유를_각각_저장한다() throws Exception {
        Order order = savedDeliveredOrder("GARAM012");
        OrderItem item1 = orderItemRepository.save(OrderItem.of(new OrderOption("black", 1), 10000, order, product));
        OrderItem item2 = orderItemRepository.save(OrderItem.of(new OrderOption("white", 1), 10000, order, product));
        given(s3FileUploader.uploadImageToS3(any(), any())).willReturn("https://cdn.test/return.jpg");
        RequestReturnRequest body = new RequestReturnRequest(ClaimReasonType.SELLER_FAULT, List.of(
            new ReturnItemRequest(item1.getId(), "사이즈 불량"),
            new ReturnItemRequest(item2.getId(), "색상 오배송")));
        MockMultipartFile request = new MockMultipartFile("request", "", MediaType.APPLICATION_JSON_VALUE,
            objectMapper.writeValueAsBytes(body));

        em.flush();
        em.clear();

        mockMvc.perform(multipart("/v1/api/refund/return")
                .file(request)
                .file(imagePart())
                .header("Authorization", userToken(user.getId())))
            .andExpect(status().isOk());

        em.flush();
        em.clear();

        Claim claim = claimRepository.findAll().get(0);
        Map<Long, String> reasonByItemId = claim.getItems().stream()
            .collect(Collectors.toMap(claimItem -> claimItem.getOrderItem().getId(), ClaimItem::getReason));
        assertThat(reasonByItemId).containsEntry(item1.getId(), "사이즈 불량").containsEntry(item2.getId(), "색상 오배송");
        assertThat(claim.getReasonType()).isEqualTo(ClaimReasonType.SELLER_FAULT);
    }

    @Test
    void 같은_상품을_중복해서_신청하면_400() throws Exception {
        Order order = savedDeliveredOrder("GARAM013");
        OrderItem item = orderItemRepository.save(OrderItem.of(new OrderOption("black", 1), 10000, order, product));

        em.flush();
        em.clear();

        mockMvc.perform(multipart("/v1/api/refund/return")
                .file(requestPart("변심", item.getId(), item.getId()))
                .file(imagePart())
                .header("Authorization", userToken(user.getId())))
            .andExpect(status().isBadRequest());

        assertThat(claimRepository.count()).isZero();
    }

    @Test
    void 서로_다른_주문의_상품을_묶어_신청하면_400() throws Exception {
        Order order1 = savedDeliveredOrder("GARAM009");
        Order order2 = savedDeliveredOrder("GARAM010");
        OrderItem item1 = orderItemRepository.save(OrderItem.of(new OrderOption("black", 1), 10000, order1, product));
        OrderItem item2 = orderItemRepository.save(OrderItem.of(new OrderOption("black", 1), 10000, order2, product));

        em.flush();
        em.clear();

        mockMvc.perform(multipart("/v1/api/refund/return")
                .file(requestPart("변심", item1.getId(), item2.getId()))
                .file(imagePart())
                .header("Authorization", userToken(user.getId())))
            .andExpect(status().isBadRequest());

        assertThat(claimRepository.count()).isZero();
    }

    @Test
    void 묶음_중_하나라도_이미_신청한_상품이면_409() throws Exception {
        Order order = savedDeliveredOrder("GARAM011");
        OrderItem item1 = orderItemRepository.save(OrderItem.of(new OrderOption("black", 1), 10000, order, product));
        OrderItem item2 = orderItemRepository.save(OrderItem.of(new OrderOption("white", 1), 10000, order, product));
        claimRepository.save(Claim.requestReturn(order, ClaimReasonType.CHANGE_OF_MIND, List.of(new ReturnItemReason(item1, "변심"))));

        em.flush();
        em.clear();

        mockMvc.perform(multipart("/v1/api/refund/return")
                .file(requestPart("변심", item1.getId(), item2.getId()))
                .file(imagePart())
                .header("Authorization", userToken(user.getId())))
            .andExpect(status().isConflict());
    }

    @Test
    void 반품_사유_유형_없이_신청시_400() throws Exception {
        Order order = savedDeliveredOrder("GARAM007");
        OrderItem item = orderItemRepository.save(OrderItem.of(new OrderOption("black", 1), 10000, order, product));

        em.flush();
        em.clear();

        mockMvc.perform(multipart("/v1/api/refund/return")
                .file(requestPart(null, "변심", item.getId()))
                .file(imagePart())
                .header("Authorization", userToken(user.getId())))
            .andExpect(status().isBadRequest());

        assertThat(claimRepository.count()).isZero();
    }

    @Test
    void 반품할_상품_없이_신청시_400() throws Exception {
        em.flush();
        em.clear();

        mockMvc.perform(multipart("/v1/api/refund/return")
                .file(requestPart("변심"))
                .file(imagePart())
                .header("Authorization", userToken(user.getId())))
            .andExpect(status().isBadRequest());
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

        mockMvc.perform(multipart("/v1/api/refund/return")
                .file(requestPart("변심", item.getId()))
                .file(imagePart())
                .header("Authorization", userToken(user.getId())))
            .andExpect(status().isBadRequest());
    }

    @Test
    void 이미_환불_신청된_주문_재신청시_409() throws Exception {
        Order order = savedDeliveredOrder("GARAM003");
        OrderItem item = orderItemRepository.save(OrderItem.of(new OrderOption("black", 1), 10000, order, product));
        claimRepository.save(Claim.requestReturn(item.getOrder(), ClaimReasonType.CHANGE_OF_MIND, List.of(new ReturnItemReason(item, "변심"))));

        em.flush();
        em.clear();

        mockMvc.perform(multipart("/v1/api/refund/return")
                .file(requestPart("변심", item.getId()))
                .file(imagePart())
                .header("Authorization", userToken(user.getId())))
            .andExpect(status().isConflict());
    }

    @Test
    void 환불_완료된_주문_재신청시_409() throws Exception {
        Order order = savedDeliveredOrder("GARAM005");
        OrderItem item = orderItemRepository.save(OrderItem.of(new OrderOption("black", 1), 10000, order, product));
        Claim completed = Claim.requestReturn(item.getOrder(), ClaimReasonType.CHANGE_OF_MIND, List.of(new ReturnItemReason(item, "변심")));
        completed.complete(FaultParty.BUYER);
        claimRepository.save(completed);

        em.flush();
        em.clear();

        mockMvc.perform(multipart("/v1/api/refund/return")
                .file(requestPart("변심", item.getId()))
                .file(imagePart())
                .header("Authorization", userToken(user.getId())))
            .andExpect(status().isConflict());
    }

    @Test
    void 환불_거절된_주문_재신청시_409() throws Exception {
        Order order = savedDeliveredOrder("GARAM006");
        OrderItem item = orderItemRepository.save(OrderItem.of(new OrderOption("black", 1), 10000, order, product));
        Claim rejected = Claim.requestReturn(item.getOrder(), ClaimReasonType.CHANGE_OF_MIND, List.of(new ReturnItemReason(item, "변심")));
        rejected.reject();
        claimRepository.save(rejected);

        em.flush();
        em.clear();

        mockMvc.perform(multipart("/v1/api/refund/return")
                .file(requestPart("변심", item.getId()))
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

        mockMvc.perform(multipart("/v1/api/refund/return")
                .file(requestPart("변심", item.getId()))
                .file(imagePart())
                .header("Authorization", userToken(otherUser.getId())))
            .andExpect(status().isForbidden());
    }

    @Test
    void 비인증_반품_신청시_401() throws Exception {
        mockMvc.perform(multipart("/v1/api/refund/return")
                .file(requestPart("변심", 999L))
                .file(imagePart()))
            .andExpect(status().isUnauthorized());
    }

    private Order savedDeliveredOrderWithFee(String orderId) {
        Order order = orderRepository.save(Order.builder()
            .orderId(orderId).user(user).totalPrice(34000).deliveryFee(4000)
            .orderAddress(address).orderStatus(OrderStatus.ORDERED).build());
        deliveryRepository.save(Delivery.builder()
            .order(order).deliveryStatus(DeliveryStatus.DELIVERED).build());
        return order;
    }

    @Test
    void 미리보기_변심_첫_반품은_주문_배송비와_반품_배송비를_차감한다() throws Exception {
        Order order = savedDeliveredOrderWithFee("GARAM101");
        OrderItem item = orderItemRepository.save(OrderItem.of(new OrderOption("black", 1), 30000, order, product));

        em.flush();
        em.clear();

        mockMvc.perform(get("/v1/api/refund/return/preview")
                .param("orderItemIds", item.getId().toString())
                .param("reasonType", "CHANGE_OF_MIND")
                .header("Authorization", userToken(user.getId())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.itemsTotalPrice").value(30000))
            .andExpect(jsonPath("$.data.orderShippingFeeDeducted").value(4000))
            .andExpect(jsonPath("$.data.returnShippingFeeDeducted").value(4000))
            .andExpect(jsonPath("$.data.expectedRefundAmount").value(22000))
            .andExpect(jsonPath("$.data.refundable").value(true))
            .andExpect(jsonPath("$.data.sellerFaultRefundAmount").doesNotExist());

        assertThat(claimRepository.count()).isZero();
    }

    @Test
    void 미리보기_여러_상품을_묶으면_배송비는_한_번만_차감한다() throws Exception {
        Order order = savedDeliveredOrderWithFee("GARAM102");
        OrderItem item1 = orderItemRepository.save(OrderItem.of(new OrderOption("black", 1), 10000, order, product));
        OrderItem item2 = orderItemRepository.save(OrderItem.of(new OrderOption("white", 1), 20000, order, product));

        em.flush();
        em.clear();

        mockMvc.perform(get("/v1/api/refund/return/preview")
                .param("orderItemIds", item1.getId().toString(), item2.getId().toString())
                .param("reasonType", "CHANGE_OF_MIND")
                .header("Authorization", userToken(user.getId())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.itemsTotalPrice").value(30000))
            .andExpect(jsonPath("$.data.expectedRefundAmount").value(22000));
    }

    @Test
    void 미리보기_판매자_귀책이면_인정될_경우의_금액도_함께_내려준다() throws Exception {
        Order order = savedDeliveredOrderWithFee("GARAM103");
        OrderItem item = orderItemRepository.save(OrderItem.of(new OrderOption("black", 1), 30000, order, product));

        em.flush();
        em.clear();

        mockMvc.perform(get("/v1/api/refund/return/preview")
                .param("orderItemIds", item.getId().toString())
                .param("reasonType", "SELLER_FAULT")
                .header("Authorization", userToken(user.getId())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.expectedRefundAmount").value(22000))
            .andExpect(jsonPath("$.data.sellerFaultRefundAmount").value(34000));
    }

    @Test
    void 미리보기_같은_주문에_완료된_반품이_있으면_반품_배송비만_차감한다() throws Exception {
        Order order = savedDeliveredOrderWithFee("GARAM104");
        OrderItem done = orderItemRepository.save(OrderItem.of(new OrderOption("black", 1), 10000, order, product));
        OrderItem item = orderItemRepository.save(OrderItem.of(new OrderOption("white", 1), 30000, order, product));
        Claim completed = Claim.requestReturn(order, ClaimReasonType.CHANGE_OF_MIND, List.of(new ReturnItemReason(done, "변심")));
        completed.complete(FaultParty.BUYER);
        claimRepository.save(completed);

        em.flush();
        em.clear();

        mockMvc.perform(get("/v1/api/refund/return/preview")
                .param("orderItemIds", item.getId().toString())
                .param("reasonType", "CHANGE_OF_MIND")
                .header("Authorization", userToken(user.getId())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.orderShippingFeeDeducted").value(0))
            .andExpect(jsonPath("$.data.returnShippingFeeDeducted").value(4000))
            .andExpect(jsonPath("$.data.expectedRefundAmount").value(26000));
    }

    @Test
    void 미리보기_차감하면_환불할_금액이_없는_상품은_refundable_false() throws Exception {
        Order order = savedDeliveredOrderWithFee("GARAM105");
        OrderItem item = orderItemRepository.save(OrderItem.of(new OrderOption("black", 1), 5000, order, product));

        em.flush();
        em.clear();

        mockMvc.perform(get("/v1/api/refund/return/preview")
                .param("orderItemIds", item.getId().toString())
                .param("reasonType", "CHANGE_OF_MIND")
                .header("Authorization", userToken(user.getId())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.expectedRefundAmount").value(0))
            .andExpect(jsonPath("$.data.refundable").value(false));
    }

    @Test
    void 미리보기_다른_유저_주문이면_403() throws Exception {
        User otherUser = userRepository.save(User.builder()
            .snsType(SnsType.KAKAO).name("다른유저").build());
        Order order = savedDeliveredOrderWithFee("GARAM106");
        OrderItem item = orderItemRepository.save(OrderItem.of(new OrderOption("black", 1), 30000, order, product));

        em.flush();
        em.clear();

        mockMvc.perform(get("/v1/api/refund/return/preview")
                .param("orderItemIds", item.getId().toString())
                .param("reasonType", "CHANGE_OF_MIND")
                .header("Authorization", userToken(otherUser.getId())))
            .andExpect(status().isForbidden());
    }

    @Test
    void 미리보기_서로_다른_주문의_상품이면_400() throws Exception {
        Order order1 = savedDeliveredOrderWithFee("GARAM107");
        Order order2 = savedDeliveredOrderWithFee("GARAM108");
        OrderItem item1 = orderItemRepository.save(OrderItem.of(new OrderOption("black", 1), 30000, order1, product));
        OrderItem item2 = orderItemRepository.save(OrderItem.of(new OrderOption("black", 1), 30000, order2, product));

        em.flush();
        em.clear();

        mockMvc.perform(get("/v1/api/refund/return/preview")
                .param("orderItemIds", item1.getId().toString(), item2.getId().toString())
                .param("reasonType", "CHANGE_OF_MIND")
                .header("Authorization", userToken(user.getId())))
            .andExpect(status().isBadRequest());
    }

    @Test
    void 미리보기_이미_신청한_상품이면_409() throws Exception {
        Order order = savedDeliveredOrderWithFee("GARAM109");
        OrderItem item = orderItemRepository.save(OrderItem.of(new OrderOption("black", 1), 30000, order, product));
        claimRepository.save(Claim.requestReturn(order, ClaimReasonType.CHANGE_OF_MIND, List.of(new ReturnItemReason(item, "변심"))));

        em.flush();
        em.clear();

        mockMvc.perform(get("/v1/api/refund/return/preview")
                .param("orderItemIds", item.getId().toString())
                .param("reasonType", "CHANGE_OF_MIND")
                .header("Authorization", userToken(user.getId())))
            .andExpect(status().isConflict());
    }

    @Test
    void 미리보기_사유_유형이_없으면_400() throws Exception {
        mockMvc.perform(get("/v1/api/refund/return/preview")
                .param("orderItemIds", "1")
                .header("Authorization", userToken(user.getId())))
            .andExpect(status().isBadRequest());
    }

    @Test
    void 미리보기_상품이_없으면_400() throws Exception {
        mockMvc.perform(get("/v1/api/refund/return/preview")
                .param("reasonType", "CHANGE_OF_MIND")
                .header("Authorization", userToken(user.getId())))
            .andExpect(status().isBadRequest());
    }

    @Test
    void 미리보기_상품이_51개를_넘으면_400() throws Exception {
        String[] ids = java.util.stream.LongStream.rangeClosed(1, 51)
            .mapToObj(String::valueOf)
            .toArray(String[]::new);

        mockMvc.perform(get("/v1/api/refund/return/preview")
                .param("orderItemIds", ids)
                .param("reasonType", "CHANGE_OF_MIND")
                .header("Authorization", userToken(user.getId())))
            .andExpect(status().isBadRequest());
    }

    @Test
    void 미리보기_비인증이면_401() throws Exception {
        mockMvc.perform(get("/v1/api/refund/return/preview")
                .param("orderItemIds", "1")
                .param("reasonType", "CHANGE_OF_MIND"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void 미리보기_판매자_귀책도_주문_배송비가_이미_정산됐으면_상품_금액만_내려준다() throws Exception {
        Order order = savedDeliveredOrderWithFee("GARAM110");
        OrderItem done = orderItemRepository.save(OrderItem.of(new OrderOption("black", 1), 10000, order, product));
        OrderItem item = orderItemRepository.save(OrderItem.of(new OrderOption("white", 1), 30000, order, product));
        Claim completed = Claim.requestReturn(order, ClaimReasonType.CHANGE_OF_MIND, List.of(new ReturnItemReason(done, "변심")));
        completed.complete(FaultParty.BUYER);
        claimRepository.save(completed);

        em.flush();
        em.clear();

        mockMvc.perform(get("/v1/api/refund/return/preview")
                .param("orderItemIds", item.getId().toString())
                .param("reasonType", "SELLER_FAULT")
                .header("Authorization", userToken(user.getId())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.expectedRefundAmount").value(26000))
            .andExpect(jsonPath("$.data.sellerFaultRefundAmount").value(30000));
    }
}
