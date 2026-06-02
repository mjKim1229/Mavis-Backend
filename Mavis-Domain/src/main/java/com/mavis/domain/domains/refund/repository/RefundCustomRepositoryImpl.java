package com.mavis.domain.domains.refund.repository;

import com.mavis.domain.domains.refund.domain.Refund;
import com.mavis.domain.domains.refund.domain.RefundStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.mavis.domain.domains.order.domain.QOrder.order;
import static com.mavis.domain.domains.order.domain.QOrderItem.orderItem;
import static com.mavis.domain.domains.product.domain.QProduct.product;
import static com.mavis.domain.domains.refund.domain.QRefund.refund;

@RequiredArgsConstructor
public class RefundCustomRepositoryImpl implements RefundCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Refund> findRefundPages(RefundStatus refundStatus, Pageable pageable) {
        List<Refund> content = queryFactory
                .selectFrom(refund)
                .join(refund.orderItem, orderItem).fetchJoin()
                .join(orderItem.order, order).fetchJoin()
                .join(orderItem.product, product).fetchJoin()
                .where(eqRefundStatus(refundStatus))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(refund.id.desc())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(refund.count())
                .from(refund)
                .where(eqRefundStatus(refundStatus));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression eqRefundStatus(RefundStatus refundStatus) {
        if (refundStatus == null) {
            return null;
        }
        return refund.refundStatus.eq(refundStatus);
    }
}
