package com.mavis.domain.domains.product.repository;

import com.mavis.common.enums.ProductCategory;
import com.mavis.common.enums.ProductSubCategory;
import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.product.vo.ColorVO;
import com.mavis.domain.domains.product.vo.ProductNoticeResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProductCustomRepository {
    List<ColorVO> getProductColors(Long productId);
    Optional<ProductNoticeResponse> getProductNoticeByProductId(Long productId);
    List<Product> getWeeklyBestProducts(LocalDate startAt, LocalDate endAt, Pageable pageable);
    List<Product> getRecentCreatedProducts(Pageable pageable);
    List<Product> getProductsByCategory(ProductCategory productCategory, ProductSubCategory subCategory, Pageable pageable);
}
