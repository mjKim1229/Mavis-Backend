package com.mavis.domain.domains.review.repository;

import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.review.domain.Review;
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
    void 리뷰_작성후_삭제되지_않은_리뷰_조회된다() {
        User user = User.builder().build();
        em.persist(user);

        Product product = Product.builder().name("테스트상품").price(10000).build();
        em.persist(product);

        Order order = Order.builder().orderId("ORDER-TEST-001").user(user).build();
        em.persist(order);

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
                .content("리뷰내용")
                .build();
        em.persist(review);

        em.flush();
        em.clear();

        assertThat(reviewRepository.findByIdAndIsDeletedFalse(review.getId())).isPresent();
    }
}
