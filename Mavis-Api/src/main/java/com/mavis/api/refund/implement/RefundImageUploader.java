package com.mavis.api.refund.implement;

import com.mavis.domain.domains.claim.domain.Claim;
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

    public void saveRefundImages(List<MultipartFile> images, Claim claim) {
        images.forEach(image -> {
            String imageUrl = fileUploader.uploadImageToS3(image, ImageDirectory.REFUND);
            claim.addImage(imageUrl);
        });
    }
}
