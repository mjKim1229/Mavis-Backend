package com.mavis.api.order.implement;

import com.mavis.api.order.dto.OrderProduct;
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

    public int saveOrderItems(List<OrderProduct> orderItemRequests, Order order) {
        int totalPrice = 0;
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderProduct orderItemRequest : orderItemRequests) {
            Product product = productReader.readById(orderItemRequest.productId());
            OrderOption option = orderItemRequest.option();
            int orderItemPrice = countPrice(option.quantity(), product.getPrice());
            totalPrice += orderItemPrice;
            OrderItem orderItem = OrderItem.of(option, orderItemPrice, order, product);
            orderItems.add(orderItem);
        }
        orderItemRepository.saveAll(orderItems);
        return totalPrice;
    }

    private int countPrice(int quantity, int price) {
        return quantity * price;
    }
}
