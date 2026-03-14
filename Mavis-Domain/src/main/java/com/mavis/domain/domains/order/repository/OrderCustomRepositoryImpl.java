package com.mavis.domain.domains.order.repository;

import com.mavis.domain.domains.delivery.domain.DeliveryStatus;
import com.mavis.domain.domains.delivery.domain.QDelivery;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.order.domain.OrderStatus;
import com.mavis.domain.domains.order.domain.QOrderItem;
import com.mavis.domain.domains.review.domain.QReview;
import com.mavis.domain.domains.user.domain.User;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.mavis.domain.domains.delivery.domain.QDelivery.delivery;
import static com.mavis.domain.domains.order.domain.QOrder.order;
import static com.mavis.domain.domains.order.domain.QOrderItem.orderItem;
import static com.mavis.domain.domains.review.domain.QReview.review;

@RequiredArgsConstructor
public class OrderCustomRepositoryImpl implements OrderCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Order> findOrderPages(Pageable pageable, OrderStatus orderStatus) {
        List<Order> orders = queryFactory.selectFrom(order)
                .where(order.isDeleted.eq(false)
                        .and(order.orderStatus.eq(orderStatus)))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(order.id.desc())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory.select(order.count())
                .from(order)
                .where(order.isDeleted.eq(false)
                        .and(order.orderStatus.eq(orderStatus)));

        return PageableExecutionUtils.getPage(orders, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<Order> findOrderPagesByUser(Pageable pageable, User user) {
        List<Order> orders = queryFactory.selectFrom(order)
                .where(order.isDeleted.eq(false)
                        .and(order.user.eq(user))
                        .and(order.orderStatus.ne(OrderStatus.READY))
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(order.id.desc())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory.select(order.count())
                .from(order)
                .where(order.isDeleted.eq(false)
                        .and(order.user.eq(user)));

        return PageableExecutionUtils.getPage(orders, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<OrderItem> findUserOrderItemCanReview(Pageable pageable, User user) {
        List<OrderItem> orderItems = queryFactory.selectFrom(orderItem)
                .join(orderItem.order, order).fetchJoin()
                .join(order.delivery, delivery).fetchJoin()
                .leftJoin(orderItem.review, review).fetchJoin()
                .where(
                        orderItem.isDeleted.eq(false)
                                .and(order.user.eq(user))
                                .and(delivery.deliveryStatus.eq(DeliveryStatus.DELIVERED))
                                .and(review.id.isNull())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(orderItem.id.desc())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory.select(orderItem.count())
                .from(orderItem)
                .join(orderItem.order, order)
                .join(order.delivery, delivery)
                .leftJoin(orderItem.review, review)
                .where(
                        orderItem.isDeleted.eq(false)
                                .and(order.user.eq(user))
                                .and(delivery.deliveryStatus.eq(DeliveryStatus.DELIVERED))
                                .and(review.id.isNull())
                );

        return PageableExecutionUtils.getPage(orderItems, pageable, countQuery::fetchOne);
    }
}
