package com.mavis.api.order.implement;

import com.mavis.api.order.dto.CreateOrderRequest;
import com.mavis.api.product.implement.ProductReader;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.order.domain.OrderOption;
import com.mavis.domain.domains.order.repository.OrderItemRepository;
import com.mavis.domain.domains.product.domain.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderItemAppender {

    private final OrderItemRepository orderItemRepository;
    private final ProductReader productReader;

    public void saveOrderItems(List<CreateOrderRequest> orderItemRequests, Order order) {
        List<OrderItem> orderItems = orderItemRequests.stream()
                .map(request -> {
                    Product product = productReader.readById(request.productId());
                    OrderOption option = request.option();
                    return OrderItem.of(option, countPrice(option.quantity(), product.getPrice()), order, product);
                })
                .toList();
        orderItemRepository.saveAll(orderItems);
    }

    private int countPrice(int quantity, int price) {
        return quantity * price;
    }
}
