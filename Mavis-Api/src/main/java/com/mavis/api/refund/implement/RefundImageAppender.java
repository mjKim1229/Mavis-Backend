package com.mavis.api.refund.implement;

import com.mavis.domain.domains.refund.domain.Refund;
import com.mavis.domain.domains.refund.domain.RefundImage;
import com.mavis.domain.domains.refund.repository.RefundImageRepository;
import com.mavis.infrastructure.image.S3FileUploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RefundImageAppender {

    private final RefundImageRepository refundImageRepository;
    private final S3FileUploader s3FileUploader;

    public void saveRefundImages(Refund refund, List<MultipartFile> images) {
        List<RefundImage> refundImages = images.stream()
                .map(image -> {
                    String imageUrl = s3FileUploader.uploadImageToS3(image);
                    return RefundImage.builder()
                            .imageUrl(imageUrl)
                            .refund(refund)
                            .build();
                })
                .toList();
        refundImageRepository.saveAll(refundImages);
    }
}
