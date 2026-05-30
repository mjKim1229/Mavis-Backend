package com.mavis.api.banner.dto;

import com.mavis.domain.domains.banner.domain.BannerImage;

public record GetBannerResponse(Long id, String imageUrl, int sortOrder) {
    public static GetBannerResponse from(BannerImage bannerImage) {
        return new GetBannerResponse(bannerImage.getId(), bannerImage.getImageUrl(), bannerImage.getSortOrder());
    }
}
