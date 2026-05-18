package com.mavis.domain.domains.product.repository;

import com.mavis.common.enums.ProductCategory;
import com.mavis.common.enums.ProductSubCategory;
import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.product.domain.ProductImageType;
import com.mavis.domain.domains.product.domain.QProductNotice;
import com.mavis.domain.domains.product.vo.ColorVO;
import com.mavis.domain.domains.product.vo.ProductNoticeResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.mavis.domain.domains.product.domain.QProduct.product;
import static com.mavis.domain.domains.product.domain.QProductColor.productColor;
import static com.mavis.domain.domains.product.domain.QProductImage.productImage;
import static com.mavis.domain.domains.product.domain.QProductTotalView.productTotalView;


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

    public List<Product> getWeeklyBestProducts(LocalDate startAt, LocalDate endAt, Pageable pageable) {
        NumberExpression<Long> totalViewsExpr = productTotalView.totalViews.sum().coalesce(0L);

        return queryFactory
                .select(product)
                .from(product)
                .leftJoin(product.totalViews, productTotalView)
                .on(productTotalView.weekStart.eq(startAt)
                        .and(productTotalView.weekEnd.eq(endAt)))
                .where(product.isDeleted.eq(false))
                .groupBy(
                        product.id,
                        product.name,
                        product.price,
                        product.subCategory,
                        product.isDeleted,
                        product.createdAt,
                        product.updatedAt
                )
                .orderBy(totalViewsExpr.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    public List<Product> getRecentCreatedProducts(Pageable pageable) {
        return queryFactory.selectFrom(product)
                .leftJoin(product.images, productImage)
                .on(productImage.imageType.eq(ProductImageType.MAIN)
                        .and(productImage.orderNum.eq(1)))
                .where(product.isDeleted.eq(false))
                .orderBy(product.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    public List<Product> getProductsByCategory(ProductCategory productCategory, ProductSubCategory subCategory, Pageable pageable) {
        return queryFactory.selectFrom(product)
                .leftJoin(product.images, productImage)
                .on(productImage.imageType.eq(ProductImageType.MAIN)
                        .and(productImage.orderNum.eq(1)))
                .where(product.isDeleted.eq(false),
                        eqProductCategory(productCategory, subCategory)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(product.createdAt.desc())
                .fetch();
    }

    @Override
    public List<Product> getClearanceProduct(Pageable pageable) {
        return queryFactory.selectFrom(product)
                .where(product.isDeleted.eq(false)
                        .and(product.isClearance.eq(true)))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(product.id.desc())
                .fetch();
    }

    @Override
    public List<Product> searchProducts(String keyword, Pageable pageable) {
        return queryFactory.selectFrom(product)
                .where(product.isDeleted.eq(false),
                        containsKeyword(keyword))
                .orderBy(product.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    private BooleanExpression containsKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;
        return product.name.containsIgnoreCase(keyword);
    }

    private BooleanExpression eqProductCategory(ProductCategory productCategory, ProductSubCategory productSubCategory) {
        if (productSubCategory == null) {
            return product.subCategory.in(productCategory.getSubCategories());
        }
        return product.subCategory.eq(productSubCategory);
    }
}
