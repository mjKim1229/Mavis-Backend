package com.mavis.api.cart.controller;

import com.mavis.api.cart.dto.CreateCartRequest;
import com.mavis.api.cart.dto.GetCartResponse;
import com.mavis.api.cart.dto.UpdateCartRequest;
import com.mavis.api.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/v1/api/cart")
@RestController
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping
    public void createCartItem(@RequestBody @Valid CreateCartRequest request) {
        cartService.createCart(request);
    }

    @PatchMapping("/{id}")
    public void updateCartItem(@PathVariable Long id, @RequestBody UpdateCartRequest request) {
        cartService.updateCartItem(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteCartItem(@PathVariable Long id) {
        cartService.delete(id);
    }

    @GetMapping
    public List<GetCartResponse> getCartResponses() {
        return cartService.getCartResponses();
    }
}
