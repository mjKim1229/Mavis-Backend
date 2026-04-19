package com.mavis.domain.domains.order.repository;

import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderStatus;
import com.mavis.domain.domains.user.domain.User;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.time.LocalDate;
import java.util.List;

import static com.mavis.domain.domains.order.domain.QOrder.order;

@RequiredArgsConstructor
public class OrderCustomRepositoryImpl implements OrderCustomRepository {

    private final JPAQueryFactory queryFactory;

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
    public Page<Order> findOrderedOrderPages(Pageable pageable, LocalDate startDate, LocalDate endDate) {
        List<Order> orders = queryFactory.selectFrom(order)
                .where(order.isDeleted.eq(false)
                        .and(order.orderStatus.eq(OrderStatus.ORDERED))
                        .and(order.createdAt.goe(startDate.atStartOfDay()))
                        .and(order.createdAt.lt(endDate.plusDays(1).atStartOfDay())))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(order.id.desc())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory.select(order.count())
                .from(order)
                .where(order.isDeleted.eq(false)
                        .and(order.orderStatus.eq(OrderStatus.ORDERED))
                        .and(order.createdAt.goe(startDate.atStartOfDay()))
                        .and(order.createdAt.lt(endDate.plusDays(1).atStartOfDay())));

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

}
