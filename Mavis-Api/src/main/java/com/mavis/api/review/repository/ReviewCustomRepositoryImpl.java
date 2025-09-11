package com.mavis.api.review.repository;

import com.mavis.api.review.domain.Review;
import com.mavis.api.review.domain.ReviewImage;
import com.mavis.api.review.dto.ProductReviewTotal;
import com.mavis.api.review.dto.ReviewResponse;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
                .join(order).on(review.order.id.eq(order.id))
                .where(order.productId.eq(productId))
                .fetchOne();
    }

    public Page<Review> queryProductReviews(Long productId, Pageable pageable) {
        JPAQuery<Review> query = queryFactory
                .selectFrom(review)
                .join(order).on(review.order.id.eq(order.id)).fetchJoin()
                .where(order.productId.eq(productId))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        for (Sort.Order order : pageable.getSort()) {
            PathBuilder<Review> entityPath = new PathBuilder<>(Review.class, "review");
            query.orderBy(new OrderSpecifier<>(
                    order.isAscending() ? Order.ASC : Order.DESC,
                    entityPath.get(order.getProperty(), Comparable.class)
            ));
        }

        List<Review> reviews = query.fetch();

        JPAQuery<Long> countQuery = queryFactory.select(review.count())
                .from(review)
                .join(order).on(review.order.id.eq(order.id))
                .where(order.productId.eq(productId));

        return PageableExecutionUtils.getPage(reviews, pageable, countQuery::fetchOne);
    }
}
