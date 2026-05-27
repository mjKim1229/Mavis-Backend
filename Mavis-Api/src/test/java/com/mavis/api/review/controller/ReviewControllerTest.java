package com.mavis.api.review.controller;

import com.mavis.api.review.dto.CreateReviewRequest;
import com.mavis.api.support.ControllerTestSupport;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.order.repository.OrderItemRepository;
import com.mavis.domain.domains.order.repository.OrderRepository;
import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.product.repository.ProductRepository;
import com.mavis.domain.domains.review.repository.ReviewImageRepository;
import com.mavis.domain.domains.user.domain.User;
import com.mavis.domain.domains.user.repository.UserRepository;
import com.mavis.infrastructure.image.ImageDirectory;
import com.mavis.infrastructure.image.S3FileUploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReviewControllerTest extends ControllerTestSupport {

    @MockitoBean S3FileUploader fileUploader;

    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private ReviewImageRepository reviewImageRepository;

    private User savedUser;
    private Product savedProduct;
    private Order savedOrder;
    private OrderItem savedOrderItem;

    @BeforeEach
    void setUp() {
        savedUser = userRepository.save(User.builder()
                .name("테스트유저")
                .email("test@test.com")
                .build());
        savedProduct = productRepository.save(Product.builder()
                .name("테스트상품")
                .price(10000)
                .build());
        savedOrder = orderRepository.save(Order.builder()
                .user(savedUser)
                .build());
        savedOrderItem = orderItemRepository.save(OrderItem.builder()
                .order(savedOrder)
                .product(savedProduct)
                .price(10000)
                .quantity(1)
                .build());
    }

    @Nested
    class 리뷰_등록 {

        @Test
        void 등록_성공() throws Exception {
            MockMultipartFile request = jsonPart("request",
                    new CreateReviewRequest("좋은 상품입니다", savedOrderItem.getId()));

            mockMvc.perform(multipart("/v1/api/review/user")
                            .file(request)
                            .with(user(savedUser.getId().toString()).roles("USER")))
                    .andExpect(status().isOk());
        }

        @Test
        void 비인증_요청시_401() throws Exception {
            MockMultipartFile request = jsonPart("request",
                    new CreateReviewRequest("좋은 상품입니다", savedOrderItem.getId()));

            mockMvc.perform(multipart("/v1/api/review/user")
                            .file(request))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void 존재하지_않는_주문상품_요청시_404() throws Exception {
            MockMultipartFile request = jsonPart("request",
                    new CreateReviewRequest("좋은 상품입니다", 999L));

            mockMvc.perform(multipart("/v1/api/review/user")
                            .file(request)
                            .with(user(savedUser.getId().toString()).roles("USER")))
                    .andExpect(status().isNotFound());
        }

        @Test
        void 다른_유저_주문상품에_리뷰_작성시_400() throws Exception {
            User otherUser = userRepository.save(User.builder()
                    .name("다른유저")
                    .email("other@test.com")
                    .build());
            Order otherOrder = orderRepository.save(Order.builder()
                    .user(otherUser)
                    .build());
            OrderItem otherOrderItem = orderItemRepository.save(OrderItem.builder()
                    .order(otherOrder)
                    .product(savedProduct)
                    .price(10000)
                    .quantity(1)
                    .build());

            MockMultipartFile request = jsonPart("request",
                    new CreateReviewRequest("좋은 상품입니다", otherOrderItem.getId()));

            mockMvc.perform(multipart("/v1/api/review/user")
                            .file(request)
                            .with(user(savedUser.getId().toString()).roles("USER")))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void 이미지_포함_등록_성공() throws Exception {
            given(fileUploader.uploadImageToS3(any(), any(ImageDirectory.class)))
                    .willReturn("https://s3.test/review/img.jpg");

            MockMultipartFile request = jsonPart("request",
                    new CreateReviewRequest("이미지 포함 리뷰", savedOrderItem.getId()));
            MockMultipartFile image = new MockMultipartFile(
                    "images", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "fake-image-bytes".getBytes());

            mockMvc.perform(multipart("/v1/api/review/user")
                            .file(request)
                            .file(image)
                            .with(user(savedUser.getId().toString()).roles("USER")))
                    .andExpect(status().isOk());

            assertThat(reviewImageRepository.findAll())
                    .hasSize(1)
                    .first()
                    .satisfies(img -> assertThat(img.getImageUrl()).isEqualTo("https://s3.test/review/img.jpg"));
        }

        private MockMultipartFile jsonPart(String name, Object value) throws Exception {
            return new MockMultipartFile(name, "", MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(value));
        }
    }
}
