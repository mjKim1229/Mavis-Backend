package com.mavis.domain.domains.order.repository;

import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderStatus;
import com.mavis.domain.domains.user.domain.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.mavis.domain.domains.order.domain.QOrder.order;

@RequiredArgsConstructor
public class OrderCusomRepositoryImpl implements OrderCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Order> findOrderLists(Pageable pageable, OrderStatus orderStatus) {
        return queryFactory.selectFrom(order)
                .where(order.isDeleted.eq(false)
                        .and(order.orderStatus.eq(orderStatus)))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(order.id.desc())
                .fetch();
    }

    @Override
    public List<Order> findOrderListByUser(Pageable pageable, OrderStatus orderStatus, User user) {
        return queryFactory.selectFrom(order)
                .where(order.isDeleted.eq(false)
                        .and(order.orderStatus.eq(orderStatus))
                        .and(order.user.eq(user)))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(order.id.desc())
                .fetch();
    }
}
