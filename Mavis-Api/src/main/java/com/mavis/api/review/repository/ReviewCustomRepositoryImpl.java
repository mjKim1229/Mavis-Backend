package com.mavis.api.review.repository;

import com.mavis.api.order.domain.QOrder;
import com.mavis.api.product.domain.QProduct;
import com.mavis.api.product.domain.QProductColor;
import com.mavis.api.review.domain.QReview;
import com.mavis.api.review.dto.ProductReviewTotal;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import static com.mavis.api.order.domain.QOrder.*;
import static com.mavis.api.product.domain.QProduct.*;
import static com.mavis.api.product.domain.QProductColor.*;
import static com.mavis.api.review.domain.QReview.*;

@RequiredArgsConstructor
public class ReviewCustomRepositoryImpl implements ReviewCustomRepository {
    private final JPAQueryFactory queryFactory;

    public ProductReviewTotal queryProductReviewTotal(Long productId) {
        return queryFactory
                .select(Projections.constructor(
                        ProductReviewTotal.class,
                        Expressions.numberTemplate(
                                Double.class, "ROUND({0}, 1)",
                                review.score.avg().coalesce(0.0)
                        ),
                        review.count()
                ))
                .from(review)
                .join(order).on(review.orderId.eq(order.id))
                .join(productColor).on(order.productColorId.eq(productColor.id))
                .join(product).on(productColor.productId.eq(product.id))
                .where(product.id.eq(productId))
                .fetchOne();
    }
}
