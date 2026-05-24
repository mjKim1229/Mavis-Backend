package com.mavis.domain.domains.delivery.repository;

import com.mavis.domain.domains.delivery.domain.Delivery;
import com.mavis.domain.domains.delivery.domain.DeliveryStatus;
import com.mavis.domain.domains.delivery.dto.AdminDeliveryRow;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.time.LocalDate;
import java.util.List;

import static com.mavis.domain.domains.delivery.domain.QDelivery.delivery;
import static com.mavis.domain.domains.order.domain.QOrder.order;
import static com.mavis.domain.domains.order.domain.QOrderItem.orderItem;
import static com.mavis.domain.domains.product.domain.QProduct.product;
import static com.mavis.domain.domains.user.domain.QUser.user;

@RequiredArgsConstructor
public class DeliveryCustomRepositoryImpl implements DeliveryCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Delivery> findDeliveryPagesByDeliveryStatus(Pageable pageable, DeliveryStatus deliveryStatus) {
        List<Delivery> deliveries = queryFactory.selectFrom(delivery)
                .where(delivery.deliveryStatus.eq(deliveryStatus))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(delivery.id.desc())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory.select(delivery.count())
                .from(delivery)
                .where(delivery.deliveryStatus.eq(deliveryStatus));

        return PageableExecutionUtils.getPage(deliveries, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<AdminDeliveryRow> findDeliveryRows(Pageable pageable, DeliveryStatus deliveryStatus) {
        List<AdminDeliveryRow> content = queryFactory
                .select(Projections.constructor(AdminDeliveryRow.class,
                        delivery.id,
                        order.orderId,
                        delivery.carrier,
                        delivery.trackingNumber,
                        delivery.deliveryStatus,
                        order.id,
                        order.orderAddress.receiverName,
                        order.orderAddress.receiverPhone,
                        order.orderAddress.address,
                        order.orderAddress.addressMemo,
                        user.name,
                        order.createdAt,
                        order.totalPrice
                ))
                .from(delivery)
                .join(delivery.order, order)
                .join(order.user, user)
                .where(delivery.deliveryStatus.eq(deliveryStatus))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(delivery.id.desc())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory.select(delivery.count())
                .from(delivery)
                .where(delivery.deliveryStatus.eq(deliveryStatus));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public List<Delivery> findDeliveriesByStatusAndDateRange(DeliveryStatus deliveryStatus, LocalDate startDate, LocalDate endDate) {
        return queryFactory.selectFrom(delivery)
                .join(delivery.order, order).fetchJoin()
                .join(order.user, user).fetchJoin()
                .join(order.orderItems, orderItem).fetchJoin()
                .join(orderItem.product, product).fetchJoin()
                .where(
                        delivery.deliveryStatus.eq(deliveryStatus),
                        order.createdAt.goe(startDate.atStartOfDay()),
                        order.createdAt.lt(endDate.plusDays(1).atStartOfDay())
                )
                .orderBy(delivery.id.desc())
                .distinct()
                .fetch();
    }
}
