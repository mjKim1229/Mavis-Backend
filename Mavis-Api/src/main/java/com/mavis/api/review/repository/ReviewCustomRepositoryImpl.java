package com.mavis.api.review.repository;

import com.mavis.api.review.dto.ProductReviewTotal;
import com.mavis.api.review.dto.ReviewResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.mavis.api.order.domain.QOrder.order;
import static com.mavis.api.product.domain.QColor.color;
import static com.mavis.api.product.domain.QProduct.product;
import static com.mavis.api.product.domain.QProductColor.productColor;
import static com.mavis.api.review.domain.QReview.review;

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

    public Page<ReviewResponse> queryProductReviews(Long productId, Pageable pageable) {
        List<ReviewResponse> reviewResponses = queryFactory.select(
                        Projections.constructor(
                                ReviewResponse.class,
                                review.id,
                                review.score,
                                review.createdAt,
                                color.name,
                                order.quantity
                        )
                )
                .from(review)
                .join(order).on(review.orderId.eq(order.id))
                .join(productColor).on(order.productColorId.eq(productColor.id))
                .join(color).on(productColor.colorId.eq(color.id))
                .join(product).on(productColor.productId.eq(product.id))
                .where(product.id.eq(productId))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(review.id.desc())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory.select(review.count())
                .from(review)
                .join(order).on(review.orderId.eq(order.id))
                .join(productColor).on(order.productColorId.eq(productColor.id))
                .join(color).on(productColor.colorId.eq(color.id))
                .join(product).on(productColor.productId.eq(product.id))
                .where(product.id.eq(productId));

        return PageableExecutionUtils.getPage(reviewResponses, pageable, countQuery::fetchCount);
    }
}
