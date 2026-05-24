package com.mavis.domain.domains.inquiry.repository;

import com.mavis.domain.domains.inquiry.domain.AnswerStatus;
import com.mavis.domain.domains.inquiry.dto.AdminInquiryRow;
import com.mavis.domain.domains.inquiry.dto.ProductInquiryRow;
import com.mavis.domain.domains.inquiry.dto.UserInquiryRow;
import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.user.domain.User;
import com.querydsl.core.types.Projections;
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
    public Page<ProductInquiryRow> findInquiryByProduct(Product product, boolean onlyUnanswered, Pageable pageable) {
        List<ProductInquiryRow> content = queryFactory
                .select(Projections.constructor(ProductInquiryRow.class,
                        inquiry.id,
                        inquiry.question,
                        inquiry.isPrivate,
                        inquiry.createdAt,
                        user.name,
                        inquiryAnswer.answer,
                        inquiryAnswer.createdAt
                ))
                .from(inquiry)
                .join(inquiry.user, user)
                .leftJoin(inquiryAnswer).on(
                        inquiryAnswer.inquiry.id.eq(inquiry.id)
                                .and(inquiryAnswer.isDeleted.eq(false))
                )
                .where(inquiry.product.id.eq(product.getId())
                        .and(inquiry.isDeleted.eq(false))
                        .and(onlyUnanswered ? unansweredCondition() : null)
                )
                .orderBy(inquiry.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory.select(inquiry.count())
                .from(inquiry)
                .leftJoin(inquiryAnswer).on(
                        inquiryAnswer.inquiry.id.eq(inquiry.id)
                                .and(inquiryAnswer.isDeleted.eq(false))
                )
                .where(inquiry.product.id.eq(product.getId())
                        .and(inquiry.isDeleted.eq(false))
                        .and(onlyUnanswered ? unansweredCondition() : null)
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression unansweredCondition() {
        return inquiryAnswer.isNull().or(inquiryAnswer.isDeleted.eq(true));
    }

    @Override
    public Page<UserInquiryRow> findInquiryByUser(User user, Pageable pageable) {
        List<UserInquiryRow> content = queryFactory
                .select(Projections.constructor(UserInquiryRow.class,
                        inquiry.id,
                        inquiry.product.id,
                        inquiry.product.name,
                        inquiry.question,
                        inquiry.createdAt,
                        inquiryAnswer.answer,
                        inquiryAnswer.createdAt
                ))
                .from(inquiry)
                .join(inquiry.product, product)
                .leftJoin(inquiryAnswer).on(
                        inquiryAnswer.inquiry.id.eq(inquiry.id)
                                .and(inquiryAnswer.isDeleted.eq(false))
                )
                .where(inquiry.user.eq(user).and(inquiry.isDeleted.eq(false)))
                .orderBy(inquiry.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory.select(inquiry.count())
                .from(inquiry)
                .where(inquiry.user.eq(user).and(inquiry.isDeleted.eq(false)));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<AdminInquiryRow> findAllInquiries(AnswerStatus status, Pageable pageable) {
        List<AdminInquiryRow> content = queryFactory
                .select(Projections.constructor(AdminInquiryRow.class,
                        inquiry.id,
                        inquiry.product.id,
                        inquiry.product.name,
                        inquiry.question,
                        user.name,
                        inquiry.createdAt,
                        inquiryAnswer.answer
                ))
                .from(inquiry)
                .join(inquiry.product, product)
                .join(inquiry.user, user)
                .leftJoin(inquiryAnswer).on(
                        inquiryAnswer.inquiry.id.eq(inquiry.id)
                                .and(inquiryAnswer.isDeleted.eq(false))
                )
                .where(inquiry.isDeleted.eq(false)
                        .and(answerStatusCondition(status))
                )
                .orderBy(inquiry.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory.select(inquiry.count())
                .from(inquiry)
                .leftJoin(inquiryAnswer).on(
                        inquiryAnswer.inquiry.id.eq(inquiry.id)
                                .and(inquiryAnswer.isDeleted.eq(false))
                )
                .where(inquiry.isDeleted.eq(false)
                        .and(answerStatusCondition(status))
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression answerStatusCondition(AnswerStatus status) {
        return switch (status) {
            case ANSWERED -> inquiryAnswer.isNotNull();
            case UNANSWERED -> inquiryAnswer.isNull();
            default -> null;
        };
    }
}
