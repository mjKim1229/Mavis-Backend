package com.mavis.domain.domains.review.repository;

import com.mavis.domain.domains.delivery.domain.DeliveryStatus;
import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.review.domain.Review;
import com.mavis.domain.domains.review.vo.ProductReviewTotal;
import com.mavis.domain.domains.user.domain.User;
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

import static com.mavis.domain.domains.delivery.domain.QDelivery.delivery;
import static com.mavis.domain.domains.order.domain.QOrder.order;
import static com.mavis.domain.domains.order.domain.QOrderItem.orderItem;
import static com.mavis.domain.domains.product.domain.QProduct.product;
import static com.mavis.domain.domains.review.domain.QReview.review;
import static com.mavis.domain.domains.review.domain.QReviewImage.reviewImage;

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

    public Page<Review> queryProductReviews(Long productId, boolean photoOnly, Pageable pageable) {
        JPAQuery<Review> query = queryFactory
                .selectFrom(review)
                .join(review.orderItem, orderItem)
                .where(orderItem.product.id.eq(productId)
                        .and(review.isDeleted.eq(false)))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        if (photoOnly) {
            query.distinct().join(review.images, reviewImage);
        }

        for (Sort.Order order : pageable.getSort()) {
            PathBuilder<Review> entityPath = new PathBuilder<>(Review.class, "review");
            query.orderBy(new OrderSpecifier<>(
                    order.isAscending() ? Order.ASC : Order.DESC,
                    entityPath.get(order.getProperty(), Comparable.class)
            ));
        }

        List<Review> reviews = query.fetch();

        JPAQuery<Long> countQuery = queryFactory.select(review.countDistinct())
                .from(review)
                .join(review.orderItem, orderItem)
                .where(orderItem.product.id.eq(productId)
                        .and(review.isDeleted.eq(false)));

        if (photoOnly) {
            countQuery.join(review.images, reviewImage);
        }

        return PageableExecutionUtils.getPage(reviews, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<Review> queryProductReviewsByUser(User user, Pageable pageable) {
        List<Review> reviews = queryFactory
                .selectFrom(review)
                .where(review.user.eq(user)
                        .and(review.isDeleted.eq(false)))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(review.count())
                .from(review)
                .where(review.user.eq(user)
                        .and(review.isDeleted.eq(false)));

        return PageableExecutionUtils.getPage(reviews, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<OrderItem> queryWritableOrderItemsByUser(User user, Pageable pageable) {
        List<OrderItem> orderItems = queryFactory.selectFrom(orderItem)
                .join(orderItem.order, order).fetchJoin()
                .join(orderItem.product, product).fetchJoin()
                .join(delivery).on(delivery.order.eq(order))
                .leftJoin(review).on(review.orderItem.eq(orderItem))
                .where(
                        order.user.eq(user),
                        delivery.deliveryStatus.eq(DeliveryStatus.DELIVERED),
                        review.id.isNull(),
                        orderItem.isDeleted.eq(false)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(orderItem.count())
                .from(orderItem)
                .join(orderItem.order, order)
                .join(delivery).on(delivery.order.eq(order))
                .leftJoin(review).on(review.orderItem.eq(orderItem))
                .where(
                        order.user.eq(user),
                        delivery.deliveryStatus.eq(DeliveryStatus.DELIVERED),
                        review.id.isNull(),
                        orderItem.isDeleted.eq(false)
                );

        return PageableExecutionUtils.getPage(orderItems, pageable, countQuery::fetchOne);
    }
}
