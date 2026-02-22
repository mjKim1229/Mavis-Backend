package com.mavis.api.cart.controller;

import com.mavis.api.cart.dto.CreateCartRequest;
import com.mavis.api.cart.dto.GetCartResponse;
import com.mavis.api.cart.dto.UpdateCartRequest;
import com.mavis.api.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/v1/api/cart")
@RestController
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @Operation(summary = "장바구니 상품 추가")
    @PostMapping
    public void createCartItem(@RequestBody @Valid CreateCartRequest request) {
        cartService.createCart(request);
    }

    @Operation(summary = "장바구니 상품 수정")
    @PatchMapping("/{id}")
    public void updateCartItem(@PathVariable Long id, @RequestBody UpdateCartRequest request) {
        cartService.updateCartItem(id, request);
    }

    @Operation(summary = "장바구니 상품 삭제")
    @DeleteMapping("/{id}")
    public void deleteCartItem(@PathVariable Long id) {
        cartService.delete(id);
    }

    @Operation(summary = "장바구니 목록 조회")
    @GetMapping
    public List<GetCartResponse> getCartResponses() {
        return cartService.getCartResponses();
    }
}
