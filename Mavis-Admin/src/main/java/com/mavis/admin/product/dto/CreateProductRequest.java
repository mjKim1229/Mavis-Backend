package com.mavis.admin.product.dto;

import com.mavis.common.enums.ProductSubCategory;
import com.mavis.domains.product.domain.Product;
import com.mavis.domains.product.domain.ProductNotice;

import java.util.List;

public record CreateProductRequest(
        String name,
        Integer price,
        ProductSubCategory subCategory,
        List<String> colors,
        ProductNoticeVO notice
) {
    public ProductNotice toProductNotice(Product product) {
        return ProductNotice.builder()
                .precaution(notice().precaution())
                .shippingInfo(notice().shippingInfo())
                .returnRequest(notice().returnRequest())
                .returnProcess(notice().returnProcess())
                .product(product)
                .build();
    }

    public Product toProduct() {
        return Product.builder()
                .name(name)
                .price(price)
                .subCategory(subCategory)
                .build();
    }
}
