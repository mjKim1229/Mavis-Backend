package com.mavis.domain.domains.product.repository;

import com.mavis.domain.domains.product.domain.*;
import com.mavis.domain.domains.product.vo.ColorVO;
import com.mavis.domain.domains.product.vo.ProductNoticeResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.mavis.domain.domains.product.domain.QProduct.*;
import static com.mavis.domain.domains.product.domain.QProductColor.productColor;
import static com.mavis.domain.domains.product.domain.QProductImage.*;
import static com.mavis.domain.domains.product.domain.QProductTotalView.*;


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
                .where(productColor.product.id.eq(productId))
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

    public List<Product> getWeeklyBestProducts(LocalDate startAt, LocalDate endAt) {
        List<Long> popularProductIds = queryFactory
                .select(productTotalView.product.id)
                .from(productTotalView)
                .where(productTotalView.weekStart.eq(startAt)
                        .and(productTotalView.weekEnd.eq(endAt)))
                .orderBy(productTotalView.totalViews.desc())
                .limit(4)
                .fetch();

        return queryFactory
                .selectDistinct(product)
                .from(product)
                .leftJoin(product.colors, productColor)
                .leftJoin(product.images, productImage)
                .where(product.id.in(popularProductIds)
                        .and(product.isDeleted.eq(false)))
                .fetch();
    }

    public List<Product> getRecentCreatedProducts() {
        return queryFactory.selectFrom(product)
                .leftJoin(product.colors, productColor)
                .leftJoin(product.images, productImage)
                .where(product.isDeleted.eq(false)
                        .and(productImage.imageType.eq(ProductImageType.MAIN)))
                .orderBy(product.createdAt.desc())
                .limit(8)
                .fetch();
    }
}
