package com.mavis.domain.domains.favorite.repository;

import com.mavis.domain.domains.favorite.domain.Favorite;
import com.mavis.domain.domains.user.domain.User;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.mavis.domain.domains.favorite.domain.QFavorite.favorite;
import static com.mavis.domain.domains.product.domain.QProduct.product;

@RequiredArgsConstructor
public class FavoriteCustomRepositoryImpl implements FavoriteCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Favorite> findUserFavorites(User user, Pageable pageable) {
        List<Favorite> content = queryFactory
                .selectFrom(favorite)
                .join(favorite.product, product).fetchJoin()
                .where(favorite.user.eq(user)
                        .and(favorite.isDeleted.eq(false)))
                .orderBy(favorite.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(favorite.count())
                .from(favorite)
                .where(favorite.user.eq(user)
                        .and(favorite.isDeleted.eq(false)));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }
}
