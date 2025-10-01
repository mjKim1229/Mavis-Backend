package com.mavis.api.cart.service;

import com.mavis.api.auth.implement.UserReader;
import com.mavis.api.cart.dto.CreateCartRequest;
import com.mavis.api.cart.dto.UpdateCartRequest;
import com.mavis.api.cart.implement.CartReader;
import com.mavis.api.product.implement.ProductReader;
import com.mavis.domain.domains.cart.domain.CartItem;
import com.mavis.domain.domains.cart.repository.CartItemRepository;
import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final UserReader userReader;
    private final ProductReader productReader;
    private final CartReader cartReader;

    @Transactional
    public void createCart(CreateCartRequest request) {
        User user = userReader.getCurrentUser();
        Product product = productReader.readById(request.productId());
        CartItem cartItem = CartItem.builder()
                .user(user)
                .product(product)
                .color(request.color())
                .quantity(request.quantity())
                .totalPrice(product.getPrice() * request.quantity())
                .build();
        cartItemRepository.save(cartItem);
    }

    @Transactional
    public void updateCartItem(Long id, UpdateCartRequest request) {
        CartItem cartItem = cartReader.findById(id);
        cartItem.update(request.quantity(), request.color());
    }

    @Transactional
    public void delete(Long id) {
        CartItem cartItem = cartReader.findById(id);
        cartItem.delete();
    }
}
