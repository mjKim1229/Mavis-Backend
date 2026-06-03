package com.mavis.api.favorite.controller;

import com.mavis.api.support.ControllerTestSupport;
import com.mavis.domain.domains.favorite.domain.Favorite;
import com.mavis.domain.domains.favorite.repository.FavoriteRepository;
import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.product.domain.ProductColor;
import com.mavis.domain.domains.product.domain.ProductImage;
import com.mavis.domain.domains.product.domain.ProductImageType;
import com.mavis.domain.domains.product.repository.ProductColorRepository;
import com.mavis.domain.domains.product.repository.ProductImageRepository;
import com.mavis.domain.domains.product.repository.ProductRepository;
import com.mavis.domain.domains.user.domain.User;
import com.mavis.domain.domains.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.hasItems;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FavoriteControllerTest extends ControllerTestSupport {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private ProductColorRepository productColorRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private EntityManager em;

    private User savedUser;
    private Product savedProduct;

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
    }

    @Test
    void 즐겨찾기_등록_성공() throws Exception {
        mockMvc.perform(post("/v1/api/favorites/{productId}", savedProduct.getId())
                        .with(user(savedUser.getId().toString()).roles("USER")))
                .andExpect(status().isOk());
    }

    @Test
    void 이미_즐겨찾기에_추가된_상품_재등록시_409() throws Exception {
        favoriteRepository.save(Favorite.builder()
                .user(savedUser)
                .product(savedProduct)
                .build());

        mockMvc.perform(post("/v1/api/favorites/{productId}", savedProduct.getId())
                        .with(user(savedUser.getId().toString()).roles("USER")))
                .andExpect(status().isConflict());
    }

    @Test
    void 비인증_즐겨찾기_등록시_401() throws Exception {
        mockMvc.perform(post("/v1/api/favorites/{productId}", savedProduct.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 즐겨찾기_삭제_성공() throws Exception {
        favoriteRepository.save(Favorite.builder()
                .user(savedUser)
                .product(savedProduct)
                .build());

        mockMvc.perform(delete("/v1/api/favorites/{productId}", savedProduct.getId())
                        .with(user(savedUser.getId().toString()).roles("USER")))
                .andExpect(status().isOk());
    }

    @Test
    void 존재하지_않는_즐겨찾기_삭제시_404() throws Exception {
        mockMvc.perform(delete("/v1/api/favorites/{productId}", savedProduct.getId())
                        .with(user(savedUser.getId().toString()).roles("USER")))
                .andExpect(status().isNotFound());
    }

    @Test
    void 비인증_즐겨찾기_삭제시_401() throws Exception {
        mockMvc.perform(delete("/v1/api/favorites/{productId}", savedProduct.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 즐겨찾기_목록_조회_JSON_필드_전체_검증() throws Exception {
        productColorRepository.save(ProductColor.of(savedProduct, "블랙"));
        productColorRepository.save(ProductColor.of(savedProduct, "화이트"));
        productImageRepository.save(ProductImage.builder()
                .product(savedProduct)
                .imageUrl("https://s3.test/main.jpg")
                .imageType(ProductImageType.MAIN)
                .orderNum(0)
                .build());
        favoriteRepository.save(Favorite.builder()
                .user(savedUser)
                .product(savedProduct)
                .build());
        em.flush();
        em.clear();

        mockMvc.perform(get("/v1/api/favorites/user")
                        .with(user(savedUser.getId().toString()).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].productId").value(savedProduct.getId()))
                .andExpect(jsonPath("$.data.content[0].productResponse.id").value(savedProduct.getId()))
                .andExpect(jsonPath("$.data.content[0].productResponse.name").value("테스트상품"))
                .andExpect(jsonPath("$.data.content[0].productResponse.price").value(10000))
                .andExpect(jsonPath("$.data.content[0].productResponse.isClearance").value(false))
                .andExpect(jsonPath("$.data.content[0].productResponse.colors").isArray())
                .andExpect(jsonPath("$.data.content[0].productResponse.colors", hasItems("블랙", "화이트")))
                .andExpect(jsonPath("$.data.content[0].productResponse.previewImage").value("https://s3.test/main.jpg"));
    }

    @Test
    void 비인증_즐겨찾기_목록_조회시_401() throws Exception {
        mockMvc.perform(get("/v1/api/favorites/user"))
                .andExpect(status().isUnauthorized());
    }
}
