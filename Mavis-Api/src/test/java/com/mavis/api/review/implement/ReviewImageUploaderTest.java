package com.mavis.api.review.implement;

import com.mavis.api.review.dto.ReviewImageVO;
import com.mavis.domain.domains.review.domain.Review;
import com.mavis.domain.domains.review.domain.ReviewImage;
import com.mavis.domain.domains.review.repository.ReviewImageRepository;
import com.mavis.infrastructure.image.S3FileUploader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ReviewImageUploaderTest {

    @InjectMocks
    private ReviewImageUploader reviewImageUploader;

    @Mock
    private S3FileUploader fileUploader;
    @Mock
    private ReviewImageRepository reviewImageRepository;
    @Mock
    private Executor s3UploadExecutor;

    @Test
    void keepImages에_없는_이미지는_소프트_삭제된다() {
        ReviewImage image1 = ReviewImage.builder().imageUrl("url1").sortOrder(0).build();
        ReviewImage image2 = ReviewImage.builder().imageUrl("url2").sortOrder(1).build();
        Review review = Review.builder().images(List.of(image1, image2)).build();

        reviewImageUploader.updateKeptImages(review, List.of(new ReviewImageVO(0, "url1")));

        assertThat(image1.isDeleted()).isFalse();
        assertThat(image2.isDeleted()).isTrue();
    }

    @Test
    void keepImages의_order로_sortOrder가_업데이트된다() {
        ReviewImage image1 = ReviewImage.builder().imageUrl("url1").sortOrder(0).build();
        ReviewImage image2 = ReviewImage.builder().imageUrl("url2").sortOrder(1).build();
        Review review = Review.builder().images(List.of(image1, image2)).build();

        List<ReviewImageVO> keepImages = List.of(
                new ReviewImageVO(1, "url1"),
                new ReviewImageVO(0, "url2")
        );

        reviewImageUploader.updateKeptImages(review, keepImages);

        assertThat(image1.getSortOrder()).isEqualTo(1);
        assertThat(image2.getSortOrder()).isEqualTo(0);
    }

    @Test
    void 이미_삭제된_이미지는_재처리하지_않는다() {
        ReviewImage deletedImage = ReviewImage.builder().imageUrl("url1").sortOrder(0).build();
        deletedImage.delete();
        Review review = Review.builder().images(List.of(deletedImage)).build();

        reviewImageUploader.updateKeptImages(review, List.of(new ReviewImageVO(5, "url1")));

        assertThat(deletedImage.getSortOrder()).isEqualTo(0);
    }

    @Test
    void keepImages가_비어있으면_모든_이미지_소프트_삭제된다() {
        ReviewImage image1 = ReviewImage.builder().imageUrl("url1").sortOrder(0).build();
        ReviewImage image2 = ReviewImage.builder().imageUrl("url2").sortOrder(1).build();
        Review review = Review.builder().images(List.of(image1, image2)).build();

        reviewImageUploader.updateKeptImages(review, List.of());

        assertThat(image1.isDeleted()).isTrue();
        assertThat(image2.isDeleted()).isTrue();
    }
}
