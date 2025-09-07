package com.mavis.api.product.repository;

import com.mavis.api.product.domain.ProductNotice;
import com.mavis.api.product.domain.QProductNotice;
import com.mavis.api.product.dto.ColorVO;
import com.mavis.api.product.dto.ProductNoticeResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

import static com.mavis.api.product.domain.QColor.color;
import static com.mavis.api.product.domain.QProductColor.productColor;
import static com.mavis.api.product.domain.QProductNotice.*;

@RequiredArgsConstructor
public class ProductCustomRepositoryImpl implements ProductCustomRepository {

    private final JPAQueryFactory queryFactory;

    public List<ColorVO> getProductColors(Long productId) {
        return queryFactory
                .select(Projections.constructor(ColorVO.class,
                        color.id,
                        color.name))
                .from(productColor)
                .join(color).on(productColor.colorId.eq(color.id))
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
