package com.mavis.domain.domains.delivery.repository;

import com.mavis.domain.domains.delivery.domain.Delivery;
import com.mavis.domain.domains.delivery.domain.DeliveryStatus;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;


import static com.mavis.domain.domains.delivery.domain.QDelivery.delivery;

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
}
