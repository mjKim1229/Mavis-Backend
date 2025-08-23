package com.mavis.api.product.repository;

import com.mavis.api.product.dto.ColorVO;

import java.util.List;

public interface ProductCustomRepository {
    List<ColorVO> getProductColors(Long productId);
}
