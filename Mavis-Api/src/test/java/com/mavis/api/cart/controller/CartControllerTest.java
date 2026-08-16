package com.mavis.api.cart.controller;

import com.mavis.api.cart.dto.CreateCartRequest;
import com.mavis.api.cart.dto.UpdateCartRequest;
import com.mavis.api.support.ControllerTestSupport;
import com.mavis.domain.domains.cart.domain.CartItem;
import com.mavis.domain.domains.cart.repository.CartItemRepository;
import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.product.repository.ProductRepository;
import com.mavis.domain.domains.user.domain.User;
import com.mavis.domain.domains.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CartControllerTest extends ControllerTestSupport {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private EntityManager em;

    private User savedUser;
    private User otherUser;
    private Product savedProduct;

    @BeforeEach
    void setUp() {
        savedUser = userRepository.save(User.builder()
                .name("테스트유저")
                .email("test@test.com")
                .build());
        otherUser = userRepository.save(User.builder()
                .name("다른유저")
                .email("other@test.com")
                .build());
        savedProduct = productRepository.save(Product.builder()
                .name("테스트상품")
                .price(10000)
                .build());
    }

    private CartItem saveCartItem(User user, int quantity, String color) {
        return cartItemRepository.save(CartItem.builder()
                .user(user)
                .product(savedProduct)
                .color(color)
                .quantity(quantity)
                .build());
    }

    @Test
    void 장바구니_상품_추가_성공() throws Exception {
        CreateCartRequest request = new CreateCartRequest(savedProduct.getId(), "블랙", 2);

        mockMvc.perform(post("/v1/api/cart")
                        .with(user(savedUser.getId().toString()).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        em.flush();
        em.clear();

        List<CartItem> items = cartItemRepository.findUserCartItem(savedUser);
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getQuantity()).isEqualTo(2);
    }

    @Test
    void 동일_상품_색상_재추가시_수량_증가() throws Exception {
        saveCartItem(savedUser, 2, "블랙");
        CreateCartRequest request = new CreateCartRequest(savedProduct.getId(), "블랙", 3);

        mockMvc.perform(post("/v1/api/cart")
                        .with(user(savedUser.getId().toString()).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        em.flush();
        em.clear();

        List<CartItem> items = cartItemRepository.findUserCartItem(savedUser);
        assertThat(items).hasSize(1);
        assertThat(items.get(0).getQuantity()).isEqualTo(5);
    }

    @Test
    void 수량_0_이하면_400() throws Exception {
        CreateCartRequest request = new CreateCartRequest(savedProduct.getId(), "블랙", 0);

        mockMvc.perform(post("/v1/api/cart")
                        .with(user(savedUser.getId().toString()).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 비인증_장바구니_추가시_401() throws Exception {
        CreateCartRequest request = new CreateCartRequest(savedProduct.getId(), "블랙", 2);

        mockMvc.perform(post("/v1/api/cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 장바구니_상품_수정_성공() throws Exception {
        CartItem cartItem = saveCartItem(savedUser, 2, "블랙");
        UpdateCartRequest request = new UpdateCartRequest("화이트", 5);

        mockMvc.perform(patch("/v1/api/cart/{id}", cartItem.getId())
                        .with(user(savedUser.getId().toString()).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        em.flush();
        em.clear();

        CartItem updated = cartItemRepository.findById(cartItem.getId()).orElseThrow();
        assertThat(updated.getColor()).isEqualTo("화이트");
        assertThat(updated.getQuantity()).isEqualTo(5);
    }

    @Test
    void 타인의_장바구니_수정시_403() throws Exception {
        CartItem cartItem = saveCartItem(otherUser, 2, "블랙");
        UpdateCartRequest request = new UpdateCartRequest("화이트", 5);

        mockMvc.perform(patch("/v1/api/cart/{id}", cartItem.getId())
                        .with(user(savedUser.getId().toString()).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void 존재하지_않는_장바구니_수정시_404() throws Exception {
        UpdateCartRequest request = new UpdateCartRequest("화이트", 5);

        mockMvc.perform(patch("/v1/api/cart/{id}", 999999L)
                        .with(user(savedUser.getId().toString()).roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void 장바구니_상품_삭제_성공() throws Exception {
        CartItem cartItem = saveCartItem(savedUser, 2, "블랙");

        mockMvc.perform(delete("/v1/api/cart/{id}", cartItem.getId())
                        .with(user(savedUser.getId().toString()).roles("USER")))
                .andExpect(status().isOk());

        em.flush();
        em.clear();

        CartItem deleted = cartItemRepository.findById(cartItem.getId()).orElseThrow();
        assertThat(deleted.isDeleted()).isTrue();
    }

    @Test
    void 타인의_장바구니_삭제시_403() throws Exception {
        CartItem cartItem = saveCartItem(otherUser, 2, "블랙");

        mockMvc.perform(delete("/v1/api/cart/{id}", cartItem.getId())
                        .with(user(savedUser.getId().toString()).roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void 존재하지_않는_장바구니_삭제시_404() throws Exception {
        mockMvc.perform(delete("/v1/api/cart/{id}", 999999L)
                        .with(user(savedUser.getId().toString()).roles("USER")))
                .andExpect(status().isNotFound());
    }

    @Test
    void 장바구니_목록_조회_성공() throws Exception {
        saveCartItem(savedUser, 2, "블랙");
        saveCartItem(savedUser, 1, "화이트");

        mockMvc.perform(get("/v1/api/cart")
                        .with(user(savedUser.getId().toString()).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void 삭제된_장바구니는_목록에서_제외() throws Exception {
        CartItem cartItem = saveCartItem(savedUser, 2, "블랙");
        cartItem.delete();
        cartItemRepository.save(cartItem);

        mockMvc.perform(get("/v1/api/cart")
                        .with(user(savedUser.getId().toString()).roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    void 비인증_장바구니_목록_조회시_401() throws Exception {
        mockMvc.perform(get("/v1/api/cart"))
                .andExpect(status().isUnauthorized());
    }
}
