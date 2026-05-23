package com.mavis.api.order.dto;

import com.mavis.common.util.DateFormatters;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.order.domain.PaymentMethod;
import com.mavis.domain.domains.refund.domain.RefundStatus;
import lombok.Builder;

import java.util.List;
import java.util.function.Function;

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
        String userName,
        String createdAt,
        String paymentMethod
) {
    public static UserOrderInfo from(Order order, Function<OrderItem, RefundStatus> refundStatusProvider) {
        return UserOrderInfo.builder()
                .orderId(order.getId())
                .tossOrderId(order.getOrderId().substring(DOMAIN_PREFIX.length()))
                .orderProductList(order.getOrderItems().stream()
                        .map(item -> OrderProduct.from(item, refundStatusProvider.apply(item)))
                        .toList())
                .orderStatusCode(order.getDisplayStatusCode())
                .orderStatus(order.getDisplayStatus())
                .address(order.getOrderAddress().getAddress())
                .addressInfo(order.getOrderAddress().getAddressDetail())
                .totalPrice(order.getTotalPrice())
                .userName(order.getUser().getName())
                .createdAt(order.getCreatedAt().format(DateFormatters.DATE_FORMATTER))
                .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null)
                .build();
    }
}
