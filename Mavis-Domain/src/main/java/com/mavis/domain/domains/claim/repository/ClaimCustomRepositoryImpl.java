package com.mavis.domain.domains.claim.repository;

import com.mavis.domain.domains.claim.domain.Claim;
import com.mavis.domain.domains.claim.domain.ClaimItem;
import com.mavis.domain.domains.claim.domain.ClaimStatus;
import com.mavis.domain.domains.claim.domain.ClaimType;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.mavis.domain.domains.claim.domain.QClaim.claim;
import static com.mavis.domain.domains.claim.domain.QClaimItem.claimItem;
import static com.mavis.domain.domains.order.domain.QOrder.order;
import static com.mavis.domain.domains.order.domain.QOrderItem.orderItem;
import static com.mavis.domain.domains.product.domain.QProduct.product;
import static com.mavis.domain.domains.user.domain.QUser.user;

@RequiredArgsConstructor
public class ClaimCustomRepositoryImpl implements ClaimCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Claim> findClaimPages(ClaimType claimType, ClaimStatus claimStatus, Pageable pageable) {
        List<Claim> content = queryFactory
                .selectFrom(claim)
                .join(claim.order, order).fetchJoin()
                .join(order.user, user).fetchJoin()
                .where(claim.claimType.eq(claimType), eqClaimStatus(claimStatus))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(claim.id.desc())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(claim.count())
                .from(claim)
                .where(claim.claimType.eq(claimType), eqClaimStatus(claimStatus));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<ClaimItem> findClaimItemPages(ClaimType claimType, Pageable pageable) {
        List<ClaimItem> content = queryFactory
                .selectFrom(claimItem)
                .join(claimItem.claim, claim).fetchJoin()
                .join(claim.order, order).fetchJoin()
                .join(order.user, user).fetchJoin()
                .join(claimItem.orderItem, orderItem).fetchJoin()
                .join(orderItem.product, product).fetchJoin()
                .where(claim.claimType.eq(claimType))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(claimItem.id.desc())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(claimItem.count())
                .from(claimItem)
                .join(claimItem.claim, claim)
                .where(claim.claimType.eq(claimType));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression eqClaimStatus(ClaimStatus claimStatus) {
        if (claimStatus == null) {
            return null;
        }
        return claim.claimStatus.eq(claimStatus);
    }
}
