package com.mavis.domain.domains.product.repository;

import com.mavis.domain.domains.product.domain.Product;

import java.time.LocalDate;

public interface ProductTotalViewCustomRepository {
    long increaseViewCount(Product product, LocalDate weekStart, LocalDate weekEnd);
}
