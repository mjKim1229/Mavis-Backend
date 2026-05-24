package com.mavis.domain.domains.order.repository;

import com.mavis.domain.domains.delivery.domain.DeliveryStatus;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.order.domain.OrderStatus;
import com.mavis.domain.domains.order.dto.AdminOrderItemRow;
import com.mavis.domain.domains.order.dto.AdminOrderRow;
import com.mavis.domain.domains.user.domain.User;
import com.querydsl.core.types.Projections;
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
import static com.mavis.domain.domains.product.domain.QProduct.product;
import static com.mavis.domain.domains.review.domain.QReview.review;
import static com.mavis.domain.domains.user.domain.QUser.user;

@RequiredArgsConstructor
public class OrderCustomRepositoryImpl implements OrderCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<AdminOrderRow> findPaymentConfirmedOrderRows(Pageable pageable) {
        List<AdminOrderRow> content = queryFactory
                .select(Projections.constructor(AdminOrderRow.class,
                        order.id,
                        order.orderId,
                        order.orderAddress.receiverName,
                        order.orderAddress.receiverPhone,
                        order.orderAddress.address,
                        order.orderAddress.addressMemo,
                        user.name,
                        order.createdAt,
                        order.totalPrice
                ))
                .from(order)
                .join(order.user, user)
                .where(order.isDeleted.eq(false)
                        .and(order.orderStatus.eq(OrderStatus.PAYMENT_CONFIRMED)))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(order.id.desc())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory.select(order.count())
                .from(order)
                .where(order.isDeleted.eq(false)
                        .and(order.orderStatus.eq(OrderStatus.PAYMENT_CONFIRMED)));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<AdminOrderRow> findOrderedOrderRows(Pageable pageable) {
        List<AdminOrderRow> content = queryFactory
                .select(Projections.constructor(AdminOrderRow.class,
                        order.id,
                        order.orderId,
                        order.orderAddress.receiverName,
                        order.orderAddress.receiverPhone,
                        order.orderAddress.address,
                        order.orderAddress.addressMemo,
                        user.name,
                        order.createdAt,
                        order.totalPrice
                ))
                .from(order)
                .join(order.user, user)
                .join(order.delivery, delivery)
                .where(order.isDeleted.eq(false)
                        .and(order.orderStatus.eq(OrderStatus.ORDERED))
                        .and(delivery.deliveryStatus.eq(DeliveryStatus.READY)))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(order.id.desc())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory.select(order.count())
                .from(order)
                .join(order.delivery, delivery)
                .where(order.isDeleted.eq(false)
                        .and(order.orderStatus.eq(OrderStatus.ORDERED))
                        .and(delivery.deliveryStatus.eq(DeliveryStatus.READY)));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public List<AdminOrderItemRow> findOrderItemRowsByOrderIds(List<Long> orderIds) {
        return queryFactory
                .select(Projections.constructor(AdminOrderItemRow.class,
                        orderItem.order.id,
                        product.name,
                        orderItem.color,
                        orderItem.quantity
                ))
                .from(orderItem)
                .join(orderItem.product, product)
                .where(orderItem.order.id.in(orderIds)
                        .and(orderItem.isDeleted.eq(false)))
                .fetch();
    }

    @Override
    public Page<Order> findPaymentConfirmedOrderPages(Pageable pageable) {
        List<Order> orders = queryFactory.selectFrom(order)
                .where(order.isDeleted.eq(false)
                        .and(order.orderStatus.eq(OrderStatus.PAYMENT_CONFIRMED)))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(order.id.desc())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory.select(order.count())
                .from(order)
                .where(order.isDeleted.eq(false)
                        .and(order.orderStatus.eq(OrderStatus.PAYMENT_CONFIRMED)));

        return PageableExecutionUtils.getPage(orders, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<Order> findOrderedOrderPages(Pageable pageable) {
        List<Order> orders = queryFactory.selectFrom(order)
                .join(order.delivery, delivery)
                .where(order.isDeleted.eq(false)
                        .and(order.orderStatus.eq(OrderStatus.ORDERED))
                        .and(delivery.deliveryStatus.eq(DeliveryStatus.READY)))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(order.id.desc())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory.select(order.count())
                .from(order)
                .join(order.delivery, delivery)
                .where(order.isDeleted.eq(false)
                        .and(order.orderStatus.eq(OrderStatus.ORDERED))
                        .and(delivery.deliveryStatus.eq(DeliveryStatus.READY)));

        return PageableExecutionUtils.getPage(orders, pageable, countQuery::fetchOne);
    }

    @Override
    public long countOrderedWithReadyDelivery() {
        Long count = queryFactory.select(order.count())
                .from(order)
                .join(order.delivery, delivery)
                .where(order.isDeleted.eq(false)
                        .and(order.orderStatus.eq(OrderStatus.ORDERED))
                        .and(delivery.deliveryStatus.eq(DeliveryStatus.READY)))
                .fetchOne();
        return count != null ? count : 0L;
    }

    @Override
    public Page<Order> findOrderPagesByUser(Pageable pageable, User user) {
        List<Order> orders = queryFactory.selectFrom(order)
                .leftJoin(order.delivery, delivery).fetchJoin()
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
                .leftJoin(review).on(review.orderItem.eq(orderItem))
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
                .leftJoin(review).on(review.orderItem.eq(orderItem))
                .where(
                        orderItem.isDeleted.eq(false)
                                .and(order.user.eq(user))
                                .and(delivery.deliveryStatus.eq(DeliveryStatus.DELIVERED))
                                .and(review.id.isNull())
                );

        return PageableExecutionUtils.getPage(orderItems, pageable, countQuery::fetchOne);
    }
}
