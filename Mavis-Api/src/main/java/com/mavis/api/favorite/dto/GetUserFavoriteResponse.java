package com.mavis.api.favorite.dto;

import com.mavis.api.product.dto.GetProductPreviewResponse;

public record GetUserFavoriteResponse(
        Long id,
        GetProductPreviewResponse productResponse
) {
}
