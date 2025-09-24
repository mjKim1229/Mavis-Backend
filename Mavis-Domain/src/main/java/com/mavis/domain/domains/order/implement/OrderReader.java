package com.mavis.domain.domains.order.implement;

import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.order.exception.OrderNotFoundException;
import com.mavis.domain.domains.order.repository.OrderItemRepository;
import com.mavis.domain.domains.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderReader {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderItem findById(Long id) {
        return orderItemRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> OrderNotFoundException.EXCEPTION);
    }
}
