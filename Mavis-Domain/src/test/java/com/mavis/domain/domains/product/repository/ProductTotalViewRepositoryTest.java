package com.mavis.domain.domains.product.repository;

import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.product.domain.ProductTotalView;
import com.mavis.domain.support.RepositoryTestSupport;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ProductTotalViewRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private ProductTotalViewRepository productTotalViewRepository;

    @PersistenceContext
    private EntityManager em;

    private static final LocalDate WEEK_START = LocalDate.of(2026, 5, 18);
    private static final LocalDate WEEK_END = LocalDate.of(2026, 5, 24);

    @Test
    void 기존_조회수_행이_있으면_1_증가한다() {
        Product product = Product.builder().name("상품").price(10000).build();
        em.persist(product);

        ProductTotalView view = ProductTotalView.builder()
                .product(product)
                .weekStart(WEEK_START)
                .weekEnd(WEEK_END)
                .build();
        em.persist(view);
        em.flush();
        em.clear();

        long updated = productTotalViewRepository.increaseViewCount(product, WEEK_START, WEEK_END);

        em.flush();
        em.clear();

        assertThat(updated).isEqualTo(1L);
        ProductTotalView result = productTotalViewRepository.findByProductAndWeekStartAndWeekEndAndIsDeletedFalse(product, WEEK_START, WEEK_END).orElseThrow();
        assertThat(result.getTotalViews()).isEqualTo(1L);
    }

    @Test
    void 조회수_행이_없으면_0을_반환한다() {
        Product product = Product.builder().name("상품").price(10000).build();
        em.persist(product);
        em.flush();
        em.clear();

        long updated = productTotalViewRepository.increaseViewCount(product, WEEK_START, WEEK_END);

        assertThat(updated).isEqualTo(0L);
    }
}
