package com.mavis.api.cart.implement;

import com.mavis.domain.domains.admin.exception.CartItemNotFoundException;
import com.mavis.domain.domains.cart.domain.CartItem;
import com.mavis.domain.domains.cart.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CartReader {

    private final CartItemRepository cartItemRepository;

    public CartItem findById(Long id) {
        return cartItemRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> CartItemNotFoundException.EXCEPTION);
    }
}
