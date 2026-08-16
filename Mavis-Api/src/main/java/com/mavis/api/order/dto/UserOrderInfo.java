package com.mavis.api.order.dto;

import com.mavis.common.util.DateFormatters;
import com.mavis.domain.domains.delivery.domain.Delivery;
import com.mavis.domain.domains.delivery.domain.DeliveryStatus;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderStatus;
import com.mavis.domain.domains.order.domain.PaymentMethod;
import lombok.Builder;

import java.util.List;

import static com.mavis.common.util.OrderNumberGenerator.DOMAIN_PREFIX;

@Builder
public record UserOrderInfo(
        Long orderId,
        String tossOrderId,
        List<OrderProduct> orderProductList,
        String orderStatusCode,
        String orderStatus,
        String address,
        String addressInfo,
        int totalPrice,
        Integer refundAmount,
        String userName,
        String createdAt,
        String paymentMethod
) {
    public static UserOrderInfo from(Order order, List<OrderProduct> orderProductList, Delivery delivery) {
        DeliveryStatus deliveryStatus = delivery != null ? delivery.getDeliveryStatus() : null;
        Integer refundAmount = order.getOrderStatus() == OrderStatus.CANCELED ? order.getTotalPrice() : null;
        return UserOrderInfo.builder()
                .orderId(order.getId())
                .tossOrderId(order.getOrderId().substring(DOMAIN_PREFIX.length()))
                .orderProductList(orderProductList)
                .orderStatusCode(order.resolveDisplayStatusCode(deliveryStatus))
                .orderStatus(order.resolveDisplayStatus(deliveryStatus))
                .address(order.getOrderAddress().getAddress())
                .addressInfo(order.getOrderAddress().getAddressDetail())
                .totalPrice(order.getTotalPrice())
                .refundAmount(refundAmount)
                .userName(order.getUser().getName())
                .createdAt(order.getCreatedAt().format(DateFormatters.DATE_FORMATTER))
                .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null)
                .build();
    }
}
