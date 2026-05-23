package com.mavis.domain.domains.product.repository;

import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.product.domain.ProductNotice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductNoticeRepository extends JpaRepository<ProductNotice, Long>, ProductCustomRepository {
    Optional<ProductNotice> findByProduct(Product product);
}
