package com.mavis.api.inquiry.implement;

import com.mavis.domain.domains.inquiry.domain.Inquiry;
import com.mavis.domain.domains.inquiry.domain.InquiryImage;
import com.mavis.domain.domains.inquiry.repository.InquiryImageRepository;
import com.mavis.infrastructure.image.S3FileUploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InquiryImageAppender {

    private final InquiryImageRepository inquiryImageRepository;
    private final S3FileUploader s3FileUploader;

    public void saveInquiryImages(Inquiry inquiry, List<MultipartFile> images) {
        List<InquiryImage> inquiryImages = images.stream()
                .map(image -> {
                    String imageUrl = s3FileUploader.uploadImageToS3(image);
                    return InquiryImage.builder()
                            .imageUrl(imageUrl)
                            .inquiry(inquiry)
                            .build();
                })
                .toList();
        inquiryImageRepository.saveAll(inquiryImages);
    }
}
