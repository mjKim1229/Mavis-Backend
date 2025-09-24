package com.mavis.domain.domains.order.implement;

import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.exception.OrderNotFoundException;
import com.mavis.domain.domains.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderReader {

    private final OrderRepository orderRepository;

    public Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> OrderNotFoundException.EXCEPTION);
    }
}
