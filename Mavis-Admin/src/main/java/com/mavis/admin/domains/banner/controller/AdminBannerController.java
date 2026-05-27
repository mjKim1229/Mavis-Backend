package com.mavis.admin.domains.banner.controller;

import com.mavis.admin.domains.banner.dto.GetBannerResponse;
import com.mavis.admin.domains.banner.dto.UpdateBannerRequest;
import com.mavis.admin.domains.banner.service.AdminBannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/admin/banners")
@Tag(name = "관리자 배너 API")
public class AdminBannerController {

    private final AdminBannerService adminBannerService;

    @GetMapping
    @Operation(summary = "배너 이미지 목록 조회")
    public List<GetBannerResponse> getBanners() {
        return adminBannerService.getBanners();
    }

    @PutMapping(consumes = "multipart/form-data")
    @Operation(summary = "배너 이미지 수정")
    public void updateBanners(@RequestPart UpdateBannerRequest request,
                              @RequestPart(required = false) List<MultipartFile> images) {
        adminBannerService.updateBanners(request, images);
    }
}
