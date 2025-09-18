package com.mavis.domains.product.repository;


import com.mavis.domains.product.domain.QProductNotice;
import com.mavis.domains.product.vo.ColorVO;
import com.mavis.domains.product.vo.ProductNoticeResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

import static com.mavis.domains.product.domain.QProductColor.productColor;

@RequiredArgsConstructor
public class ProductCustomRepositoryImpl implements ProductCustomRepository {

    private final JPAQueryFactory queryFactory;

    public List<ColorVO> getProductColors(Long productId) {
        return queryFactory
                .select(Projections.constructor(
                        ColorVO.class,
                        productColor.color)
                )
                .from(productColor)
                .where(productColor.productId.eq(productId))
                .fetch();
    }

    public Optional<ProductNoticeResponse> getProductNoticeByProductId(Long productId) {
        ProductNoticeResponse productNotice = queryFactory
                .select(Projections.constructor(
                                ProductNoticeResponse.class,
                                Expressions.as(Expressions.constant(productId), "productId"),
                                QProductNotice.productNotice.precaution,
                                QProductNotice.productNotice.shippingInfo,
                                QProductNotice.productNotice.returnRequest,
                                QProductNotice.productNotice.returnProcess
                        )
                ).from(QProductNotice.productNotice)
                .where(QProductNotice.productNotice.product.id.eq(productId))
                .fetchOne();

        return Optional.ofNullable(productNotice);
    }
}
