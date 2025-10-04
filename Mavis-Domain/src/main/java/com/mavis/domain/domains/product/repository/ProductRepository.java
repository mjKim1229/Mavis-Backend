package com.mavis.domain.domains.product.repository;

import com.mavis.domain.domains.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductCustomRepository {
    Optional<Product> findByIdAndIsDeletedFalse(Long id);
}
