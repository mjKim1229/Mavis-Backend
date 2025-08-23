package com.mavis.api.product.repository;

import com.mavis.api.product.dto.ColorVO;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.mavis.api.product.domain.QColor.color;
import static com.mavis.api.product.domain.QProductColor.productColor;

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
}
