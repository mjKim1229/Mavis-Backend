package com.mavis.domains.product.repository;

import com.mavis.domains.product.domain.ProductNotice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductNoticeRepository extends JpaRepository<ProductNotice, Long>, ProductCustomRepository {
}
