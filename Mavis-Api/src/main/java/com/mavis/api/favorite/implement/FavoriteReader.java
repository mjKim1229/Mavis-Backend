package com.mavis.api.favorite.implement;

import com.mavis.domain.domains.favorite.domain.Favorite;
import com.mavis.domain.domains.favorite.exception.FavoriteNotFoundException;
import com.mavis.domain.domains.favorite.repository.FavoriteRepository;
import com.mavis.domain.domains.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FavoriteReader {

    private final FavoriteRepository favoriteRepository;

    public Favorite readById(Long favoriteId) {
        return favoriteRepository.findByIdAndIsDeletedFalse(favoriteId)
                .orElseThrow(() -> FavoriteNotFoundException.EXCEPTION);
    }

    public Favorite readByUserAndProductId(User user, Long productId) {
        return favoriteRepository.findByUserAndProductIdAndIsDeletedFalse(user, productId)
                .orElseThrow(() -> FavoriteNotFoundException.EXCEPTION);
    }
}
