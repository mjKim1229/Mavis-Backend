package com.mavis.domain.domains.product.repository;

import com.mavis.domain.domains.product.domain.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
}
