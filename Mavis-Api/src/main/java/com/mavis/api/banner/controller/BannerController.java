package com.mavis.api.banner.controller;

import com.mavis.api.banner.dto.GetBannerResponse;
import com.mavis.api.banner.service.BannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/banners")
@Tag(name = "배너 API")
public class BannerController {

    private final BannerService bannerService;

    @GetMapping
    @Operation(summary = "메인 배너 이미지 목록 조회")
    public List<GetBannerResponse> getBanners() {
        return bannerService.getBanners();
    }
}
