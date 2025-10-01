package com.mavis.domain.domains.cart.repository;

import com.mavis.domain.domains.cart.domain.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long>, CartItemCustomRepository{
    Optional<CartItem> findByIdAndIsDeletedFalse(Long id);
}
