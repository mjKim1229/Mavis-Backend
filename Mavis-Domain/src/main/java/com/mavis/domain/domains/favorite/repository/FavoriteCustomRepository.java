package com.mavis.domain.domains.favorite.repository;

import com.mavis.domain.domains.favorite.domain.Favorite;
import com.mavis.domain.domains.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FavoriteCustomRepository {
    Page<Favorite> findUserFavorites(User user, Pageable pageable);
}
