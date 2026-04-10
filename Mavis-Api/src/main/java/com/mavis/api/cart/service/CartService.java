package com.mavis.api.cart.service;

import com.mavis.api.auth.implement.UserReader;
import com.mavis.api.cart.dto.CreateCartRequest;
import com.mavis.api.cart.dto.GetCartResponse;
import com.mavis.api.cart.dto.UpdateCartRequest;
import com.mavis.api.cart.implement.CartReader;
import com.mavis.domain.domains.product.implement.ProductReader;
import com.mavis.domain.domains.cart.domain.CartItem;
import com.mavis.domain.domains.cart.exception.UnauthorizedCartException;
import com.mavis.domain.domains.cart.repository.CartItemRepository;
import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        User currentUser = userReader.getCurrentUser();
        CartItem cartItem = cartReader.findById(id);
        if (!cartItem.getUser().getId().equals(currentUser.getId())) {
            throw UnauthorizedCartException.EXCEPTION;
        }
        cartItem.update(request.quantity(), request.color());
    }

    @Transactional
    public void delete(Long id) {
        User currentUser = userReader.getCurrentUser();
        CartItem cartItem = cartReader.findById(id);
        if (!cartItem.getUser().getId().equals(currentUser.getId())) {
            throw UnauthorizedCartException.EXCEPTION;
        }
        cartItem.delete();
    }

    @Transactional(readOnly = true)
    public List<GetCartResponse> getCartResponses() {
        User user = userReader.getCurrentUser();
        List<CartItem> cartItems = cartItemRepository.findUserCartItem(user);
        return cartItems.stream()
                .map(cartItem -> GetCartResponse.from(cartItem, cartItem.getProduct()))
                .toList();
    }
}
