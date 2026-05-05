package com.mavis.domain.domains.cart.repository;

import com.mavis.domain.domains.cart.domain.CartItem;
import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long>, CartItemCustomRepository{
    Optional<CartItem> findByIdAndIsDeletedFalse(Long id);
    Optional<CartItem> findByUserAndProductAndColorAndIsDeletedFalse(User user, Product product, String color);
}
