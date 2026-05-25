package com.mavis.api.review.implement;

import com.mavis.api.review.dto.ReviewImageVO;
import com.mavis.domain.domains.review.domain.Review;
import com.mavis.domain.domains.review.domain.ReviewImage;
import com.mavis.domain.domains.review.repository.ReviewImageRepository;
import com.mavis.infrastructure.image.ImageDirectory;
import com.mavis.infrastructure.image.S3FileUploader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    public void updateKeptImages(Review review, List<ReviewImageVO> keepImages) {
        Map<String, ReviewImageVO> keepMap = keepImages.stream()
                .collect(Collectors.toMap(ReviewImageVO::imageUrl, vo -> vo));

        review.getImages().stream()
                .filter(image -> !image.isDeleted())
                .filter(image -> !keepMap.containsKey(image.getImageUrl()))
                .forEach(ReviewImage::delete);

        review.getImages().stream()
                .filter(image -> !image.isDeleted())
                .filter(image -> keepMap.containsKey(image.getImageUrl()))
                .forEach(image -> image.update(keepMap.get(image.getImageUrl()).order()));
    }

    public void saveReviewImages(List<MultipartFile> images, Review review, int startOrder) {
        if (images == null || images.isEmpty()) return;
        List<CompletableFuture<ReviewImage>> futures = IntStream.range(0, images.size())
                .mapToObj(i -> CompletableFuture.supplyAsync(
                        () -> {
                            String imageUrl = fileUploader.uploadImageToS3(images.get(i), ImageDirectory.REVIEW);
                            return ReviewImage.builder()
                                    .imageUrl(imageUrl)
                                    .review(review)
                                    .sortOrder(startOrder + i)
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

    public void saveReviewImages(List<MultipartFile> images, Review review) {
        saveReviewImages(images, review, 0);
    }
}
