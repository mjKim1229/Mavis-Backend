package com.mavis.api.refund.implement;

import com.mavis.domain.domains.refund.domain.Refund;
import com.mavis.domain.domains.refund.domain.RefundImage;
import com.mavis.domain.domains.refund.repository.RefundImageRepository;
import com.mavis.infrastructure.image.ImageDirectory;
import com.mavis.infrastructure.image.S3FileUploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RefundImageUploader {

    private final S3FileUploader fileUploader;
    private final RefundImageRepository refundImageRepository;

    public void saveRefundImages(List<MultipartFile> images, Refund refund) {
        List<RefundImage> refundImages = images.stream()
                .map(image -> {
                    String imageUrl = fileUploader.uploadImageToS3(image, ImageDirectory.REFUND);
                    return RefundImage.builder()
                            .imageUrl(imageUrl)
                            .refund(refund)
                            .build();
                })
                .toList();

        refundImageRepository.saveAll(refundImages);
    }
}
