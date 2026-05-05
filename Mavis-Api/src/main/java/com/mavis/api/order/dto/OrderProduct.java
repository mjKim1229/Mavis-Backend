package com.mavis.api.order.dto;

import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.order.domain.OrderOption;
import com.mavis.domain.domains.product.domain.ProductImage;
import com.mavis.domain.domains.product.domain.ProductImageType;
import com.mavis.domain.domains.refund.domain.RefundStatus;

import java.util.Comparator;

public record OrderProduct(
        Long orderItemId,
        Long productId,
        String productName,
        OrderOption option,
        int totalPrice,
        RefundStatus refundStatus,
        String productImageUrl
) {
    public static OrderProduct from(OrderItem orderItem) {
        RefundStatus refundStatus = orderItem.getRefund() != null
                ? orderItem.getRefund().getRefundStatus()
                : null;
        String imageUrl = orderItem.getProduct().getImages().stream()
                .filter(img -> img.getImageType() == ProductImageType.MAIN)
                .min(Comparator.comparingInt(ProductImage::getOrderNum))
                .map(ProductImage::getImageUrl)
                .orElse(null);
        return new OrderProduct(
                orderItem.getId(),
                orderItem.getProduct().getId(),
                orderItem.getProduct().getName(),
                new OrderOption(orderItem.getColor(), orderItem.getQuantity()),
                orderItem.getPrice() * orderItem.getQuantity(),
                refundStatus,
                imageUrl
        );
    }
}
