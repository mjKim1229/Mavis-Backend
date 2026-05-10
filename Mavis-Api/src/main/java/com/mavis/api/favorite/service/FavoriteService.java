package com.mavis.api.favorite.service;

import com.mavis.api.auth.implement.UserReader;
import com.mavis.api.common.page.PageResponse;
import com.mavis.api.favorite.dto.GetUserFavoriteResponse;
import com.mavis.api.favorite.implement.FavoriteReader;
import com.mavis.api.product.dto.GetProductPreviewResponse;
import com.mavis.domain.domains.favorite.domain.Favorite;
import com.mavis.domain.domains.favorite.exception.UnauthorizedFavoriteException;
import com.mavis.domain.domains.favorite.repository.FavoriteRepository;
import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.product.implement.ProductReader;
import com.mavis.domain.domains.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final UserReader userReader;
    private final FavoriteRepository favoriteRepository;
    private final ProductReader productReader;
    private final FavoriteReader favoriteReader;

    @Transactional(readOnly = true)
    public PageResponse<GetUserFavoriteResponse> getUserFavorites(Pageable pageable) {
        User user = userReader.getCurrentUser();
        Page<GetUserFavoriteResponse> userFavoritePages = favoriteRepository.findByUserAndIsDeletedFalse(user, pageable)
                .map(favorite -> {
                    Product product = favorite.getProduct();
                    List<String> colors = product.getColorNames();
                    String previewImage = product.getMainImageUrl();
                    GetProductPreviewResponse productResponse = GetProductPreviewResponse.from(product, colors, previewImage);
                    return new GetUserFavoriteResponse(favorite.getId(), productResponse);
                });
        return PageResponse.of(userFavoritePages);
    }

    @Transactional
    public void createFavorite(Long productId) {
        User user = userReader.getCurrentUser();
        Product product = productReader.readById(productId);
        Favorite favorite = Favorite.builder()
                .user(user)
                .product(product)
                .build();
        favoriteRepository.save(favorite);
    }

    @Transactional
    public void deleteFavorite(Long favoriteId) {
        User currentUser = userReader.getCurrentUser();
        Favorite favorite = favoriteReader.readById(favoriteId);
        if (!favorite.getUser().getId().equals(currentUser.getId())) {
            throw UnauthorizedFavoriteException.EXCEPTION;
        }
        favorite.delete();
    }
}
