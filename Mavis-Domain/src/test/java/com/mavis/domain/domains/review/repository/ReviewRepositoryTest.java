package com.mavis.domain.domains.review.repository;

import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.review.domain.Review;
import com.mavis.domain.domains.review.vo.ProductReviewTotal;
import com.mavis.domain.domains.user.domain.User;
import com.mavis.domain.support.RepositoryTestSupport;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private ReviewRepository reviewRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    void 소수점_평균점수가_첫째자리에서_반올림된다() {
        // given: 4 + 5 + 4 = 13 / 3 = 4.333... → ROUND(4.333, 1) = 4.3
        User user = User.builder().build();
        em.persist(user);

        Product product = Product.builder().name("테스트상품").price(10000).build();
        em.persist(product);

        Order order = Order.builder().orderId("ORDER-TEST-001").user(user).build();
        em.persist(order);

        saveReviewWithScore(user, product, order, 4);
        saveReviewWithScore(user, product, order, 5);
        saveReviewWithScore(user, product, order, 4);

        em.flush();
        em.clear();

        // when
        ProductReviewTotal result = reviewRepository.queryProductReviewTotal(product.getId());

        // then
        assertThat(result.averageScore()).isEqualTo(4.3);
        assertThat(result.count()).isEqualTo(3L);
    }

    @Test
    void 리뷰가_없으면_평균_0점_개수_0개를_반환한다() {
        // given
        Product product = Product.builder().name("리뷰없는상품").price(5000).build();
        em.persist(product);
        em.flush();
        em.clear();

        // when
        ProductReviewTotal result = reviewRepository.queryProductReviewTotal(product.getId());

        // then
        assertThat(result.averageScore()).isEqualTo(0.0);
        assertThat(result.count()).isEqualTo(0L);
    }

    private void saveReviewWithScore(User user, Product product, Order order, int score) {
        OrderItem orderItem = OrderItem.builder()
                .product(product)
                .order(order)
                .price(10000)
                .quantity(1)
                .color("BLACK")
                .build();
        em.persist(orderItem);

        Review review = Review.builder()
                .orderItem(orderItem)
                .user(user)
                .score(score)
                .content("리뷰내용")
                .build();
        em.persist(review);
    }
}
