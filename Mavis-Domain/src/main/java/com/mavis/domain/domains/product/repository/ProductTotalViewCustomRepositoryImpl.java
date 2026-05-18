package com.mavis.domain.domains.product.repository;

import com.mavis.domain.domains.product.domain.Product;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

import static com.mavis.domain.domains.product.domain.QProductTotalView.productTotalView;

@RequiredArgsConstructor
public class ProductTotalViewCustomRepositoryImpl implements ProductTotalViewCustomRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public long increaseViewCount(Product product, LocalDate weekStart, LocalDate weekEnd) {
        return queryFactory.update(productTotalView)
                .set(productTotalView.totalViews, productTotalView.totalViews.add(1))
                .where(productTotalView.product.eq(product)
                        .and(productTotalView.weekStart.eq(weekStart))
                        .and(productTotalView.weekEnd.eq(weekEnd))
                        .and(productTotalView.isDeleted.eq(false)))
                .execute();
    }
}
