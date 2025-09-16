package com.mavis.domains.product.repository;

import com.mavis.domains.product.domain.ProductColor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductColorRepository extends JpaRepository<ProductColor, Long>, ProductCustomRepository {
}
