package com.mavis.domain.domains.inquiry.repository;

import com.mavis.domain.domains.inquiry.domain.AnswerStatus;
import com.mavis.domain.domains.inquiry.domain.Inquiry;
import com.mavis.domain.domains.user.domain.User;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.mavis.domain.domains.inquiry.domain.QInquiry.inquiry;
import static com.mavis.domain.domains.inquiry.domain.QInquiryAnswer.inquiryAnswer;
import static com.mavis.domain.domains.product.domain.QProduct.product;
import static com.mavis.domain.domains.user.domain.QUser.user;

@RequiredArgsConstructor
public class InquiryCustomRepositoryImpl implements InquiryCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Inquiry> findInquiryByProductId(Long productId, boolean onlyUnanswered, Pageable pageable) {
        List<Inquiry> inquiryList = queryFactory.select(inquiry)
                .from(inquiry)
                .join(inquiry.user, user).fetchJoin()
                .leftJoin(inquiryAnswer).on(inquiryAnswer.inquiry.eq(inquiry))
                .where(inquiry.product.id.eq(productId)
                        .and(inquiry.isDeleted.eq(false))
                        .and(onlyUnanswered ? unansweredCondition() : null)
                )
                .orderBy(inquiry.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory.select(inquiry.count())
                .from(inquiry)
                .leftJoin(inquiryAnswer).on(inquiryAnswer.inquiry.eq(inquiry))
                .where(inquiry.product.id.eq(productId)
                        .and(inquiry.isDeleted.eq(false))
                        .and(onlyUnanswered ? unansweredCondition() : null)
                );

        return PageableExecutionUtils.getPage(inquiryList, pageable, countQuery::fetchOne);
    }

    private BooleanExpression unansweredCondition() {
        return inquiryAnswer.isNull().or(inquiryAnswer.isDeleted.eq(true));
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

    @Override
    public Page<Inquiry> findAllInquiries(AnswerStatus status, Pageable pageable) {
        List<Inquiry> inquiryList = queryFactory.select(inquiry)
                .from(inquiry)
                .join(inquiry.product, product).fetchJoin()
                .join(inquiry.user, user).fetchJoin()
                .leftJoin(inquiryAnswer).on(inquiryAnswer.inquiry.eq(inquiry))
                .where(inquiry.isDeleted.eq(false)
                        .and(answerStatusCondition(status))
                )
                .orderBy(inquiry.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory.select(inquiry.count())
                .from(inquiry)
                .leftJoin(inquiryAnswer).on(inquiryAnswer.inquiry.eq(inquiry))
                .where(inquiry.isDeleted.eq(false)
                        .and(answerStatusCondition(status))
                );

        return PageableExecutionUtils.getPage(inquiryList, pageable, countQuery::fetchOne);
    }

    private BooleanExpression answerStatusCondition(AnswerStatus status) {
        return switch (status) {
            case ANSWERED -> inquiryAnswer.isNotNull().and(inquiryAnswer.isDeleted.eq(false));
            case UNANSWERED -> inquiryAnswer.isNull().or(inquiryAnswer.isDeleted.eq(true));
            default -> null;
        };
    }
}
