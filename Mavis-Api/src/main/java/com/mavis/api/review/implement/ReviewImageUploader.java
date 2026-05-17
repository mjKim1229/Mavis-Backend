package com.mavis.api.review.implement;

import com.mavis.domain.domains.review.domain.Review;
import com.mavis.domain.domains.review.domain.ReviewImage;
import com.mavis.domain.domains.review.repository.ReviewImageRepository;
import com.mavis.infrastructure.image.ImageDirectory;
import com.mavis.infrastructure.image.S3FileUploader;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Component
public class ReviewImageUploader {

    private final S3FileUploader fileUploader;
    private final ReviewImageRepository reviewImageRepository;
    private final Executor s3UploadExecutor;

    public ReviewImageUploader(S3FileUploader fileUploader,
                               ReviewImageRepository reviewImageRepository,
                               @Qualifier("s3UploadExecutor") Executor s3UploadExecutor) {
        this.fileUploader = fileUploader;
        this.reviewImageRepository = reviewImageRepository;
        this.s3UploadExecutor = s3UploadExecutor;
    }

    public void deleteRemovedImages(Review review, List<String> keepImageUrls) {
        Set<String> keepSet = Set.copyOf(keepImageUrls);
        List<ReviewImage> toDelete = review.getImages().stream()
                .filter(image -> !keepSet.contains(image.getImageUrl()))
                .toList();
        reviewImageRepository.deleteAll(toDelete);
    }

    public void saveReviewImages(List<MultipartFile> images, Review review) {
        List<CompletableFuture<ReviewImage>> futures = images.stream()
                .map(image -> CompletableFuture.supplyAsync(
                        () -> {
                            String imageUrl = fileUploader.uploadImageToS3(image, ImageDirectory.REVIEW);
                            return ReviewImage.builder()
                                    .imageUrl(imageUrl)
                                    .review(review)
                                    .build();
                        },
                        s3UploadExecutor
                ))
                .toList();

        List<ReviewImage> reviewImages = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        reviewImageRepository.saveAll(reviewImages);
    }
}
