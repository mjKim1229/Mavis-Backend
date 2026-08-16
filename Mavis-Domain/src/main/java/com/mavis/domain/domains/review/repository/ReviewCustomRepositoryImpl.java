package com.mavis.domain.domains.review.repository;

import com.mavis.domain.domains.delivery.domain.DeliveryStatus;
import com.mavis.domain.domains.product.domain.ProductImageType;
import com.mavis.domain.domains.review.domain.Review;
import com.mavis.domain.domains.review.vo.GetWritableUserOrderItemResponseVO;
import com.mavis.domain.domains.user.domain.User;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPAExpressions;
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
import static com.mavis.domain.domains.product.domain.QProductImage.productImage;
import static com.mavis.domain.domains.review.domain.QReview.review;
import static com.mavis.domain.domains.review.domain.QReviewImage.reviewImage;
import static com.mavis.domain.domains.user.domain.QUser.user;

@RequiredArgsConstructor
public class ReviewCustomRepositoryImpl implements ReviewCustomRepository {
    private final JPAQueryFactory queryFactory;

    public Page<Review> queryProductReviews(Long productId, boolean photoOnly, Pageable pageable) {
        JPAQuery<Review> query = queryFactory
            .selectFrom(review)
            .join(review.orderItem, orderItem).fetchJoin()
            .join(review.user, user).fetchJoin()
            .where(orderItem.product.id.eq(productId)
                .and(review.isDeleted.eq(false)))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize());

        if (photoOnly) {
            query.where(hasNonDeletedImage());
        }

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
            .join(review.orderItem, orderItem)
            .where(orderItem.product.id.eq(productId)
                .and(review.isDeleted.eq(false)));

        if (photoOnly) {
            countQuery.where(hasNonDeletedImage());
        }

        return PageableExecutionUtils.getPage(reviews, pageable, countQuery::fetchOne);
    }

    private BooleanExpression hasNonDeletedImage() {
        return JPAExpressions.selectOne()
            .from(reviewImage)
            .where(reviewImage.review.eq(review)
                .and(reviewImage.isDeleted.eq(false)))
            .exists();
    }

    @Override
    public Page<Review> queryProductReviewsByUser(User user, Pageable pageable) {
        List<Review> reviews = queryFactory
            .selectFrom(review)
            .join(review.orderItem, orderItem).fetchJoin()
            .join(orderItem.product, product).fetchJoin()
            .where(review.user.eq(user)
                .and(review.isDeleted.eq(false)))
            .orderBy(review.createdAt.desc())
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
    public Page<GetWritableUserOrderItemResponseVO> queryWritableOrderItemsByUser(User user, Pageable pageable) {
        List<GetWritableUserOrderItemResponseVO> content = queryFactory
            .select(Projections.constructor(GetWritableUserOrderItemResponseVO.class,
                orderItem.id,
                product.name,
                orderItem.totalPrice,
                orderItem.color,
                orderItem.quantity,
                productImage.imageUrl
            ))
            .from(orderItem)
            .join(orderItem.order, order)
            .join(orderItem.product, product)
            .leftJoin(productImage).on(
                productImage.product.eq(product),
                productImage.imageType.eq(ProductImageType.MAIN),
                productImage.isDeleted.eq(false)
            )
            .join(delivery).on(delivery.order.id.eq(order.id))
            .leftJoin(review).on(review.orderItem.id.eq(orderItem.id))
            .where(
                order.user.eq(user),
                delivery.deliveryStatus.eq(DeliveryStatus.DELIVERED),
                review.id.isNull(),
                orderItem.isDeleted.eq(false)
            )
            .orderBy(orderItem.id.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        JPAQuery<Long> countQuery = queryFactory
            .select(orderItem.count())
            .from(orderItem)
            .join(orderItem.order, order)
            .join(delivery).on(delivery.order.id.eq(order.id))
            .leftJoin(review).on(review.orderItem.id.eq(orderItem.id))
            .where(
                order.user.eq(user),
                delivery.deliveryStatus.eq(DeliveryStatus.DELIVERED),
                review.id.isNull(),
                orderItem.isDeleted.eq(false)
            );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }
}
