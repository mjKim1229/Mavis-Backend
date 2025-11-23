package com.mavis.domain.domains.inquiry.repository;

import com.mavis.domain.domains.inquiry.domain.Inquiry;
import com.mavis.domain.domains.user.domain.User;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.mavis.domain.domains.inquiry.domain.QInquiry.inquiry;

@RequiredArgsConstructor
public class InquiryCustomRepositoryImpl implements InquiryCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Inquiry> findInquiryByProductId(Long productId, Pageable pageable) {
        List<Inquiry> inquiryList = queryFactory.select(inquiry)
                .from(inquiry)
                .where(inquiry.product.id.eq(productId)
                        .and(inquiry.isDeleted.eq(false))
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory.select(inquiry.count())
                .from(inquiry)
                .where(inquiry.product.id.eq(productId)
                        .and(inquiry.isDeleted.eq(false))
                );

        return PageableExecutionUtils.getPage(inquiryList, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<Inquiry> findInquiryByUser(User user, Pageable pageable) {
        List<Inquiry> inquiryList = queryFactory.select(inquiry)
                .from(inquiry)
                .where(inquiry.user.eq(user)
                        .and(inquiry.isDeleted.eq(false))
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory.select(inquiry.count())
                .from(inquiry)
                .where(inquiry.user.eq(user)
                        .and(inquiry.isDeleted.eq(false))
                );

        return PageableExecutionUtils.getPage(inquiryList, pageable, countQuery::fetchOne);
    }
}
