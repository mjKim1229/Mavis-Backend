package com.mavis.api.product.repository;

import com.mavis.api.product.domain.ProductNotice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductNoticeRepository extends JpaRepository<ProductNotice, Long>, ProductCustomRepository {
}
