package com.mavis.domain.domains.product.repository;

import com.mavis.domain.domains.product.domain.ProductNotice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductNoticeRepository extends JpaRepository<ProductNotice, Long>, ProductCustomRepository {
}
