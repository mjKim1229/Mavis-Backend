package com.mavis.api.product.repository;

import com.mavis.api.product.domain.ProductColor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductColorRepository extends JpaRepository<ProductColor, Long>, ProductCustomRepository {
}
