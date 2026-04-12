package com.mavis.api.review.implement;

import com.mavis.domain.domains.review.domain.Review;
import com.mavis.domain.domains.review.domain.ReviewImage;
import com.mavis.domain.domains.review.repository.ReviewImageRepository;
import com.mavis.infrastructure.image.S3FileUploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ReviewImageUploader {

    private final S3FileUploader fileUploader;
    private final ReviewImageRepository reviewImageRepository;

    public void deleteRemovedImages(Review review, List<String> keepImageUrls) {
        Set<String> keepSet = Set.copyOf(keepImageUrls);
        List<ReviewImage> toDelete = review.getImages().stream()
                .filter(image -> !keepSet.contains(image.getImageUrl()))
                .toList();
        reviewImageRepository.deleteAll(toDelete);
    }

    public void saveReviewImages(List<MultipartFile> images, Review review) {
        List<ReviewImage> reviewImages = images.stream()
                .map(
                        image -> {
                            String imageUrl = fileUploader.uploadImageToS3(image);
                            return ReviewImage.builder()
                                    .imageUrl(imageUrl)
                                    .review(review)
                                    .build();
                        }
                )
                .toList();

        reviewImageRepository.saveAll(reviewImages);
    }
}
