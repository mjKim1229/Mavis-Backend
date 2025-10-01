package com.mavis.domain.domains.cart.repository;

import com.mavis.domain.domains.cart.domain.CartItem;
import com.mavis.domain.domains.user.domain.User;

import java.util.List;

public interface CartItemCustomRepository {
    List<CartItem> findUserCartItem(User user);
}
