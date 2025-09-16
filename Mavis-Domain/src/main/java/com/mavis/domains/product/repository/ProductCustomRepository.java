package com.mavis.domains.product.repository;

import com.mavis.domains.product.vo.ColorVO;
import com.mavis.domains.product.vo.ProductNoticeResponse;

import java.util.List;
import java.util.Optional;

public interface ProductCustomRepository {
    List<ColorVO> getProductColors(Long productId);
    Optional<ProductNoticeResponse> getProductNoticeByProductId(Long productId);
}
