package com.mavis.admin.domains.banner.service;

import com.mavis.admin.domains.banner.dto.BannerImageVO;
import com.mavis.admin.domains.banner.dto.GetBannerResponse;
import com.mavis.admin.domains.banner.dto.UpdateBannerRequest;
import com.mavis.domain.domains.banner.domain.BannerImage;
import com.mavis.domain.domains.banner.repository.BannerImageRepository;
import com.mavis.infrastructure.image.ImageDirectory;
import com.mavis.infrastructure.image.S3FileUploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class AdminBannerService {

    private final BannerImageRepository bannerImageRepository;
    private final S3FileUploader s3FileUploader;

    @Transactional(readOnly = true)
    public List<GetBannerResponse> getBanners() {
        List<BannerImage> bannerImages = bannerImageRepository.findAllByIsDeletedFalseOrderBySortOrderAsc();
        return bannerImages.stream().map(GetBannerResponse::from).toList();
    }

    @Transactional
    public void updateBanners(UpdateBannerRequest request, List<MultipartFile> images) {
        List<BannerImageVO> keepImages = request.keepImages() != null ? request.keepImages() : List.of();
        Map<String, BannerImageVO> keepMap = keepImages.stream()
                .collect(Collectors.toMap(BannerImageVO::imageUrl, vo -> vo));

        List<BannerImage> existing = bannerImageRepository.findAllByIsDeletedFalseOrderBySortOrderAsc();
        existing.stream()
                .filter(image -> !keepMap.containsKey(image.getImageUrl()))
                .forEach(BannerImage::delete);
        existing.stream()
                .filter(image -> keepMap.containsKey(image.getImageUrl()))
                .forEach(image -> image.update(keepMap.get(image.getImageUrl()).order()));

        if (images != null && !images.isEmpty()) {
            int maxOrder = keepImages.stream().mapToInt(BannerImageVO::order).max().orElse(-1);
            List<BannerImage> newImages = IntStream.range(0, images.size())
                    .mapToObj(i -> {
                        String imageUrl = s3FileUploader.uploadImageToS3(images.get(i), ImageDirectory.BANNER);
                        return BannerImage.builder()
                                .imageUrl(imageUrl)
                                .sortOrder(maxOrder + 1 + i)
                                .build();
                    })
                    .toList();
            bannerImageRepository.saveAll(newImages);
        }
    }
}
