package com.mavis.api.favorite.api;

import com.mavis.api.common.page.PageResponse;
import com.mavis.api.favorite.dto.GetUserFavoriteResponse;
import com.mavis.api.favorite.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping("/user")
    public PageResponse<GetUserFavoriteResponse> getUserFavorites(Pageable pageable) {
        return favoriteService.getUserFavorites(pageable);
    }

    @PostMapping("/{productId}")
    public void createFavorites(@PathVariable Long productId) {
        favoriteService.createFavorite(productId);
    }

    @DeleteMapping("/{favoriteId}")
    public void deleteFavorite(@PathVariable Long favoriteId) {
        favoriteService.deleteFavorite(favoriteId);
    }
}
