package com.mavis.domain.domains.cart.repository;

import com.mavis.domain.domains.cart.domain.CartItem;
import com.mavis.domain.domains.cart.domain.QCartItem;
import com.mavis.domain.domains.product.domain.QProduct;
import com.mavis.domain.domains.user.domain.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.mavis.domain.domains.cart.domain.QCartItem.cartItem;
import static com.mavis.domain.domains.product.domain.QProduct.product;

@RequiredArgsConstructor
public class CartItemCustomRepositoryImpl implements CartItemCustomRepository{

    private final JPAQueryFactory queryFactory;

    public List<CartItem> findUserCartItem(User user) {
        return queryFactory.selectFrom(cartItem)
                .join(cartItem.product, product).fetchJoin()
                .where(cartItem.user.id.eq(user.getId())
                        .and(cartItem.isDeleted.eq(false))
                )
                .fetch();
    }
}
