package com.mavis.admin.domains.banner.dto;

import java.util.List;

public record UpdateBannerRequest(List<BannerImageVO> keepImages) {
}
