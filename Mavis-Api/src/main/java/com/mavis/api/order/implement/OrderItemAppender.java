package com.mavis.api.order.implement;

import com.mavis.api.order.dto.OrderItemRequest;
import com.mavis.domain.domains.product.implement.ProductReader;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.order.domain.OrderOption;
import com.mavis.domain.domains.order.repository.OrderItemRepository;
import com.mavis.domain.domains.product.domain.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderItemAppender {

    private final OrderItemRepository orderItemRepository;
    private final ProductReader productReader;

    public int saveOrderItems(List<OrderItemRequest> orderItemRequests, Order order) {
        int totalPrice = 0;
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemRequest orderItemRequest : orderItemRequests) {
            Product product = productReader.readById(orderItemRequest.productId());
            OrderOption option = orderItemRequest.option();
            int unitPrice = product.getPrice();
            int orderItemPrice = countPrice(option.quantity(), unitPrice);
            totalPrice += orderItemPrice;
            OrderItem orderItem = OrderItem.of(option, unitPrice, orderItemPrice, order, product);
            orderItems.add(orderItem);
        }
        orderItemRepository.saveAll(orderItems);
        return totalPrice;
    }

    private int countPrice(int quantity, int price) {
        return quantity * price;
    }
}
