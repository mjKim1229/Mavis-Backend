package com.mavis.api.banner.service;

import com.mavis.api.banner.dto.GetBannerResponse;
import com.mavis.domain.domains.banner.repository.BannerImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BannerService {

    private final BannerImageRepository bannerImageRepository;

    @Transactional(readOnly = true)
    public List<GetBannerResponse> getBanners() {
        return bannerImageRepository.findAllByIsDeletedFalseOrderBySortOrderAsc()
                .stream()
                .map(GetBannerResponse::from)
                .toList();
    }
}
