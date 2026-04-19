package com.mavis.api.order.implement;

import com.mavis.domain.domains.delivery.domain.Delivery;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderStatus;
import org.springframework.stereotype.Component;

@Component
public class OrderStatusResolver {

    public String resolve(Order order, Delivery delivery) {
        if (order.getOrderStatus() == OrderStatus.CANCELED) {
            return order.getOrderStatus().getTitle();
        }
        if (delivery != null) {
            return delivery.getDeliveryStatus().getTitle();
        }
        return order.getOrderStatus().getTitle();
    }
}
