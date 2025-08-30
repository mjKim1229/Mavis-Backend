package com.mavis.api.product.repository;

import com.mavis.api.product.dto.ColorVO;
import com.mavis.api.product.dto.ProductNoticeResponse;

import java.util.List;
import java.util.Optional;

public interface ProductCustomRepository {
    List<ColorVO> getProductColors(Long productId);
    Optional<ProductNoticeResponse> getProductNoticeByProductId(Long productId);
}
