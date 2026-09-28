package com.mavis.api.order.dto;

import com.mavis.common.util.DateFormatters;
import com.mavis.domain.domains.delivery.domain.Delivery;
import com.mavis.domain.domains.delivery.domain.DeliveryStatus;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.PaymentMethod;
import com.mavis.domain.domains.refund.domain.Refund;
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
        int deliveryFee,
        Integer refundAmount,
        Integer refundShippingFee,
        String userName,
        String createdAt,
        String paymentMethod,
        String paymentMethodTitle
) {
    // cancelRefund: 주문 취소로 나간 환불 (취소 주문이 아니면 null). 배송 후 반품은 OrderProduct.refundAmount(상품 단위)로 노출
    public static UserOrderInfo from(Order order, List<OrderProduct> orderProductList, Delivery delivery, Refund cancelRefund) {
        DeliveryStatus deliveryStatus = delivery != null ? delivery.getDeliveryStatus() : null;
        Integer refundAmount = cancelRefund != null ? cancelRefund.getTotalAmount() : null;
        Integer refundShippingFee = cancelRefund != null ? cancelRefund.getShippingFeeRefund() : null;
        PaymentMethod paymentMethod = order.getPaymentMethod();
        return UserOrderInfo.builder()
                .orderId(order.getId())
                .tossOrderId(order.getOrderId().substring(DOMAIN_PREFIX.length()))
                .orderProductList(orderProductList)
                .orderStatusCode(order.resolveDisplayStatusCode(deliveryStatus))
                .orderStatus(order.resolveDisplayStatus(deliveryStatus))
                .address(order.getOrderAddress().getAddress())
                .addressInfo(order.getOrderAddress().getAddressDetail())
                .totalPrice(order.getTotalPrice())
                .deliveryFee(order.getDeliveryFee())
                .refundAmount(refundAmount)
                .refundShippingFee(refundShippingFee)
                .userName(order.getUser().getName())
                .createdAt(order.getCreatedAt().format(DateFormatters.DATE_FORMATTER))
                .paymentMethod(paymentMethod != null ? paymentMethod.name() : null)
                .paymentMethodTitle(paymentMethod != null ? paymentMethod.getKr() : null)
                .build();
    }
}
