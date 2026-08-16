package com.mavis.api.review.controller;

import com.mavis.api.review.dto.CreateReviewRequest;
import com.mavis.api.support.ControllerTestSupport;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.order.repository.OrderItemRepository;
import com.mavis.domain.domains.order.repository.OrderRepository;
import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.product.repository.ProductRepository;
import com.mavis.domain.domains.review.domain.Review;
import com.mavis.domain.domains.review.domain.ReviewImage;
import com.mavis.domain.domains.review.repository.ReviewImageRepository;
import com.mavis.domain.domains.review.repository.ReviewRepository;
import com.mavis.domain.domains.user.domain.User;
import com.mavis.domain.domains.user.repository.UserRepository;
import com.mavis.infrastructure.image.ImageDirectory;
import com.mavis.infrastructure.image.S3FileUploader;
import jakarta.persistence.EntityManager;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReviewControllerTest extends ControllerTestSupport {

    @MockitoBean S3FileUploader fileUploader;

    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private ReviewImageRepository reviewImageRepository;
    @Autowired private ReviewRepository reviewRepository;
    @Autowired private EntityManager em;

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
                .totalPrice(10000)
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
                    .totalPrice(10000)
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

    @Nested
    class 상품_리뷰_목록_조회 {

        private OrderItem createOrderItem(Product product, String color, int quantity) {
            return orderItemRepository.save(OrderItem.builder()
                    .order(savedOrder)
                    .product(product)
                    .totalPrice(10000)
                    .color(color)
                    .quantity(quantity)
                    .build());
        }

        private Review createReview(OrderItem orderItem, String content) {
            return reviewRepository.save(Review.builder()
                    .content(content)
                    .orderItem(orderItem)
                    .user(savedUser)
                    .build());
        }

        private void createReviewImage(Review review, String imageUrl, int sortOrder) {
            reviewImageRepository.save(ReviewImage.builder()
                    .review(review)
                    .imageUrl(imageUrl)
                    .sortOrder(sortOrder)
                    .build());
        }

        @Test
        void 목록_조회_JSON_필드_전체_검증() throws Exception {
            OrderItem orderItem = createOrderItem(savedProduct, "블랙", 2);
            Review review = createReview(orderItem, "좋은 상품입니다");
            createReviewImage(review, "https://s3.test/review/1.jpg", 0);
            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/review/product/{productId}", savedProduct.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.content[0].reviewId").value(review.getId()))
                    .andExpect(jsonPath("$.data.content[0].content").value("좋은 상품입니다"))
                    .andExpect(jsonPath("$.data.content[0].color").value("블랙"))
                    .andExpect(jsonPath("$.data.content[0].quantity").value(2))
                    .andExpect(jsonPath("$.data.content[0].name").value("테****"))
                    .andExpect(jsonPath("$.data.content[0].createdAt").isNotEmpty())
                    .andExpect(jsonPath("$.data.content[0].imageUrls").isArray())
                    .andExpect(jsonPath("$.data.content[0].imageUrls[0]").value("https://s3.test/review/1.jpg"));
        }

        @Test
        void photoOnly_true시_사진있는_리뷰만_조회() throws Exception {
            OrderItem withImageItem = createOrderItem(savedProduct, "블랙", 1);
            Review withImage = createReview(withImageItem, "사진 리뷰");
            createReviewImage(withImage, "https://s3.test/review/photo.jpg", 0);

            OrderItem noImageItem = createOrderItem(savedProduct, "화이트", 1);
            createReview(noImageItem, "텍스트만 리뷰");
            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/review/product/{productId}", savedProduct.getId())
                            .param("photoOnly", "true"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.content[0].content").value("사진 리뷰"))
                    .andExpect(jsonPath("$.data.content[0].imageUrls[0]").value("https://s3.test/review/photo.jpg"));
        }

        @Test
        void photoOnly_기본값_false시_사진없는_리뷰도_포함() throws Exception {
            OrderItem withImageItem = createOrderItem(savedProduct, "블랙", 1);
            Review withImage = createReview(withImageItem, "사진 리뷰");
            createReviewImage(withImage, "https://s3.test/review/photo.jpg", 0);

            OrderItem noImageItem = createOrderItem(savedProduct, "화이트", 1);
            createReview(noImageItem, "텍스트만 리뷰");
            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/review/product/{productId}", savedProduct.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(2));
        }

        @Test
        void photoOnly_true시_이미지_전부_삭제된_리뷰_제외() throws Exception {
            OrderItem orderItem = createOrderItem(savedProduct, "블랙", 1);
            Review review = createReview(orderItem, "이미지 삭제된 리뷰");
            ReviewImage image = reviewImageRepository.save(ReviewImage.builder()
                    .review(review)
                    .imageUrl("https://s3.test/review/deleted.jpg")
                    .sortOrder(0)
                    .build());
            image.delete();
            reviewImageRepository.save(image);
            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/review/product/{productId}", savedProduct.getId())
                            .param("photoOnly", "true"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(0));
        }

        @Test
        void 삭제된_리뷰_제외() throws Exception {
            OrderItem orderItem = createOrderItem(savedProduct, "블랙", 1);
            Review review = createReview(orderItem, "삭제될 리뷰");
            review.delete();
            reviewRepository.save(review);
            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/review/product/{productId}", savedProduct.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(0));
        }

        @Test
        void 다른_상품_리뷰_제외() throws Exception {
            Product otherProduct = productRepository.save(Product.builder()
                    .name("다른상품")
                    .price(20000)
                    .build());
            OrderItem otherItem = createOrderItem(otherProduct, "블랙", 1);
            createReview(otherItem, "다른 상품 리뷰");
            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/review/product/{productId}", savedProduct.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(0));
        }

        @Test
        void 리뷰_없을때_빈_목록_반환() throws Exception {
            mockMvc.perform(get("/v1/api/review/product/{productId}", savedProduct.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(0))
                    .andExpect(jsonPath("$.data.content").isEmpty());
        }
    }

    @Nested
    class 내_리뷰_목록_조회 {

        private OrderItem createOrderItem(String color, int quantity) {
            return orderItemRepository.save(OrderItem.builder()
                    .order(savedOrder)
                    .product(savedProduct)
                    .totalPrice(10000)
                    .color(color)
                    .quantity(quantity)
                    .build());
        }

        private Review createReview(User user, OrderItem orderItem, String content) {
            return reviewRepository.save(Review.builder()
                    .content(content)
                    .orderItem(orderItem)
                    .user(user)
                    .build());
        }

        private void createReviewImage(Review review, String imageUrl, int sortOrder) {
            reviewImageRepository.save(ReviewImage.builder()
                    .review(review)
                    .imageUrl(imageUrl)
                    .sortOrder(sortOrder)
                    .build());
        }

        @Test
        void 내_리뷰_목록_조회_JSON_필드_전체_검증() throws Exception {
            OrderItem orderItem = createOrderItem("블랙", 2);
            Review review = createReview(savedUser, orderItem, "내가 쓴 리뷰");
            createReviewImage(review, "https://s3.test/review/my.jpg", 0);
            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/review/user")
                            .with(user(savedUser.getId().toString()).roles("USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.content[0].reviewId").value(review.getId()))
                    .andExpect(jsonPath("$.data.content[0].content").value("내가 쓴 리뷰"))
                    .andExpect(jsonPath("$.data.content[0].createdAt").isNotEmpty())
                    .andExpect(jsonPath("$.data.content[0].imageUrls[0]").value("https://s3.test/review/my.jpg"))
                    .andExpect(jsonPath("$.data.content[0].orderItemInfo.productName").value("테스트상품"))
                    .andExpect(jsonPath("$.data.content[0].orderItemInfo.color").value("블랙"))
                    .andExpect(jsonPath("$.data.content[0].orderItemInfo.quantity").value(2));
        }

        @Test
        void 본인_리뷰만_조회() throws Exception {
            User otherUser = userRepository.save(User.builder()
                    .name("다른유저")
                    .email("other@test.com")
                    .build());
            OrderItem myItem = createOrderItem("블랙", 1);
            OrderItem otherItem = createOrderItem("화이트", 1);
            createReview(savedUser, myItem, "내 리뷰");
            createReview(otherUser, otherItem, "남의 리뷰");
            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/review/user")
                            .with(user(savedUser.getId().toString()).roles("USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(1))
                    .andExpect(jsonPath("$.data.content[0].content").value("내 리뷰"));
        }

        @Test
        void 삭제된_리뷰_제외() throws Exception {
            OrderItem orderItem = createOrderItem("블랙", 1);
            Review review = createReview(savedUser, orderItem, "삭제될 리뷰");
            review.delete();
            reviewRepository.save(review);
            em.flush();
            em.clear();

            mockMvc.perform(get("/v1/api/review/user")
                            .with(user(savedUser.getId().toString()).roles("USER")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(0));
        }

        @Test
        void 비인증_요청시_401() throws Exception {
            mockMvc.perform(get("/v1/api/review/user"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
