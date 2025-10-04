package com.mavis.domain.domains.product.repository;

import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.product.domain.ProductTotalView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface ProductTotalViewRepository extends JpaRepository<ProductTotalView, Long> {
    Optional<ProductTotalView> findByProductAndWeekStartAndWeekEndAndIsDeletedFalse(Product product, LocalDate startAt, LocalDate endAt);
}
