package com.mavis.domain.domains.inquiry.repository;

import com.mavis.domain.domains.inquiry.domain.Inquiry;
import com.mavis.domain.domains.inquiry.domain.QInquiry;
import com.mavis.domain.domains.inquiry.domain.QInquiryAnswer;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.mavis.domain.domains.inquiry.domain.QInquiry.*;
import static com.mavis.domain.domains.inquiry.domain.QInquiryAnswer.*;

@RequiredArgsConstructor
public class InquiryCustomRepositoryImpl implements InquiryCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Inquiry> findInquiryByProductId(Long productId) {
        return queryFactory.select(inquiry)
                .from(inquiry)
                .leftJoin(inquiryAnswer).on(inquiryAnswer.inquiry.id.eq(inquiry.id))
                .where(inquiry.product.id.eq(productId)
                                .and(inquiry.isDeleted.eq(false))
                )
                .fetch();
    }
}
