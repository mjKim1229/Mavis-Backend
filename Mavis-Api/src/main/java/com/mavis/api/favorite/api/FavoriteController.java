package com.mavis.api.favorite.api;

import com.mavis.api.common.page.PageResponse;
import com.mavis.api.favorite.dto.GetUserFavoriteResponse;
import com.mavis.api.favorite.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @Operation(summary = "즐겨찾기 목록 조회")
    @GetMapping("/user")
    public PageResponse<GetUserFavoriteResponse> getUserFavorites(Pageable pageable) {
        return favoriteService.getUserFavorites(pageable);
    }

    @Operation(summary = "즐겨찾기 등록")
    @PostMapping("/{productId}")
    public void createFavorites(@PathVariable Long productId) {
        favoriteService.createFavorite(productId);
    }

    @Operation(summary = "즐겨찾기 삭제")
    @DeleteMapping("/{favoriteId}")
    public void deleteFavorite(@PathVariable Long favoriteId) {
        favoriteService.deleteFavorite(favoriteId);
    }
}
