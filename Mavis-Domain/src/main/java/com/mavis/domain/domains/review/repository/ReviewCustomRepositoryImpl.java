package com.mavis.domain.domains.review.repository;

import com.mavis.domain.domains.review.domain.Review;
import com.mavis.domain.domains.review.vo.ProductReviewTotal;
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

import static com.mavis.domain.domains.order.domain.QOrderItem.orderItem;
import static com.mavis.domain.domains.review.domain.QReview.review;

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
                .join(orderItem).on(review.orderItem.id.eq(orderItem.id))
                .where(orderItem.product.id.eq(productId))
                .fetchOne();
    }

    public Page<Review> queryProductReviews(Long productId, Pageable pageable) {
        JPAQuery<Review> query = queryFactory
                .selectFrom(review)
                .join(orderItem).on(review.orderItem.id.eq(orderItem.id)).fetchJoin()
                .where(orderItem.product.id.eq(productId))
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
                .join(orderItem).on(review.orderItem.id.eq(orderItem.id))
                .where(orderItem.product.id.eq(productId));

        return PageableExecutionUtils.getPage(reviews, pageable, countQuery::fetchOne);
    }
}
