package com.mavis.api.review.service;

import com.mavis.api.review.dto.ReviewImageVO;
import com.mavis.api.review.dto.UpdateReviewRequest;
import com.mavis.api.review.dto.UserReviewResponse;
import com.mavis.api.review.implement.ReviewImageUploader;
import com.mavis.api.review.implement.ReviewValidator;
import com.mavis.api.auth.implement.UserReader;
import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.order.implement.OrderReader;
import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.product.implement.ProductReader;
import com.mavis.domain.domains.review.domain.Review;
import com.mavis.domain.domains.review.exception.ReviewNotFoundException;
import com.mavis.domain.domains.review.exception.UnauthorizedReviewException;
import com.mavis.domain.domains.review.repository.ReviewRepository;
import com.mavis.domain.domains.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private ReviewImageUploader reviewImageUploader;
    @Mock
    private OrderReader orderReader;
    @Mock
    private UserReader userReader;
    @Mock
    private ReviewValidator reviewValidator;
    @Mock
    private ProductReader productReader;

    @Test
    void 리뷰_단건_조회_성공() {
        Long reviewId = 1L;
        User user = User.builder().id(1L).name("홍길동").build();
        Product product = Product.builder().name("상품명").price(10000).build();
        OrderItem orderItem = mock(OrderItem.class);
        Review review = mock(Review.class);

        given(userReader.getCurrentUser()).willReturn(user);
        given(reviewRepository.findByIdAndIsDeletedFalse(reviewId)).willReturn(Optional.of(review));
        given(review.getUser()).willReturn(user);
        given(review.getId()).willReturn(reviewId);
        given(review.getCreatedAt()).willReturn(LocalDateTime.now());
        given(review.getImages()).willReturn(List.of());
        given(review.getOrderItem()).willReturn(orderItem);
        given(orderItem.getProduct()).willReturn(product);
        given(orderItem.getColor()).willReturn("BLACK");
        given(orderItem.getQuantity()).willReturn(1);

        UserReviewResponse response = reviewService.getUserReviewById(reviewId);

        assertThat(response.reviewId()).isEqualTo(reviewId);
        assertThat(response.imageUrls()).isEmpty();
    }

    @Test
    void 존재하지_않는_리뷰_조회시_ReviewNotFoundException_던진다() {
        Long reviewId = 999L;
        User user = User.builder().id(1L).build();

        given(userReader.getCurrentUser()).willReturn(user);
        given(reviewRepository.findByIdAndIsDeletedFalse(reviewId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.getUserReviewById(reviewId))
                .isInstanceOf(ReviewNotFoundException.class);
    }

    @Test
    void 다른_사용자의_리뷰_조회시_UnauthorizedReviewException_던진다() {
        Long reviewId = 1L;
        User currentUser = User.builder().id(1L).build();
        User reviewOwner = User.builder().id(2L).build();
        Review review = mock(Review.class);

        given(userReader.getCurrentUser()).willReturn(currentUser);
        given(reviewRepository.findByIdAndIsDeletedFalse(reviewId)).willReturn(Optional.of(review));
        given(review.getUser()).willReturn(reviewOwner);

        assertThatThrownBy(() -> reviewService.getUserReviewById(reviewId))
                .isInstanceOf(UnauthorizedReviewException.class);
    }

    @Test
    void 리뷰_수정시_keepImages_기반으로_이미지_업데이트된다() {
        Long reviewId = 1L;
        User user = User.builder().id(1L).build();
        Review review = mock(Review.class);

        given(userReader.getCurrentUser()).willReturn(user);
        given(reviewRepository.findByIdAndIsDeletedFalse(reviewId)).willReturn(Optional.of(review));
        given(review.getUser()).willReturn(user);

        List<ReviewImageVO> keepImages = List.of(
                new ReviewImageVO(0, "url1"),
                new ReviewImageVO(1, "url2")
        );
        UpdateReviewRequest request = new UpdateReviewRequest("수정내용", false, keepImages);

        reviewService.updateReview(reviewId, request, null);

        verify(reviewImageUploader).updateKeptImages(review, keepImages);
        verify(reviewImageUploader, never()).saveReviewImages(any(), any(), anyInt());
    }

    @Test
    void 새_이미지_추가시_keepImages의_최대_순서_이후부터_시작한다() {
        Long reviewId = 1L;
        User user = User.builder().id(1L).build();
        Review review = mock(Review.class);
        MultipartFile newImage = mock(MultipartFile.class);

        given(userReader.getCurrentUser()).willReturn(user);
        given(reviewRepository.findByIdAndIsDeletedFalse(reviewId)).willReturn(Optional.of(review));
        given(review.getUser()).willReturn(user);

        List<ReviewImageVO> keepImages = List.of(
                new ReviewImageVO(0, "url1"),
                new ReviewImageVO(2, "url2")
        );
        UpdateReviewRequest request = new UpdateReviewRequest("수정내용", false, keepImages);

        reviewService.updateReview(reviewId, request, List.of(newImage));

        verify(reviewImageUploader).saveReviewImages(List.of(newImage), review, 3);
    }
}
