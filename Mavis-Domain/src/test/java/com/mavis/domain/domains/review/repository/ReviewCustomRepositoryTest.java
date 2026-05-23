package com.mavis.domain.domains.review.repository;

import com.mavis.domain.domains.delivery.domain.Delivery;
import com.mavis.domain.domains.delivery.domain.DeliveryStatus;
import com.mavis.domain.domains.order.domain.Order;
import com.mavis.domain.domains.order.domain.OrderItem;
import com.mavis.domain.domains.product.domain.Product;
import com.mavis.domain.domains.product.domain.ProductImage;
import com.mavis.domain.domains.product.domain.ProductImageType;
import com.mavis.domain.domains.review.domain.Review;
import com.mavis.domain.domains.review.vo.GetWritableUserOrderItemResponseVO;
import com.mavis.domain.domains.user.domain.User;
import com.mavis.domain.support.RepositoryTestSupport;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewCustomRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private ReviewRepository reviewRepository;

    @PersistenceContext
    private EntityManager em;

    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        user = User.builder().build();
        product = Product.builder().name("테스트상품").price(10000).build();
        em.persist(user);
        em.persist(product);
        em.persist(ProductImage.builder()
                .product(product)
                .imageUrl("http://img.test/main.jpg")
                .imageType(ProductImageType.MAIN)
                .orderNum(0)
                .build());
        em.flush();
    }

    @Test
    void 배송완료_리뷰없는_주문상품_조회된다() {
        Order order = Order.builder().orderId("ORDER-001").user(user).build();
        em.persist(order);
        em.persist(Delivery.builder().order(order).deliveryStatus(DeliveryStatus.DELIVERED).build());
        em.persist(OrderItem.builder().product(product).order(order).price(10000).quantity(1).color("BLACK").build());
        em.flush();
        em.clear();

        Page<GetWritableUserOrderItemResponseVO> result =
                reviewRepository.queryWritableOrderItemsByUser(user, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void 배송준비중이면_조회안된다() {
        Order order = Order.builder().orderId("ORDER-002").user(user).build();
        em.persist(order);
        em.persist(Delivery.builder().order(order).deliveryStatus(DeliveryStatus.READY).build());
        em.persist(OrderItem.builder().product(product).order(order).price(10000).quantity(1).color("BLACK").build());
        em.flush();
        em.clear();

        Page<GetWritableUserOrderItemResponseVO> result =
                reviewRepository.queryWritableOrderItemsByUser(user, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void 이미_리뷰_작성한_주문상품은_조회안된다() {
        Order order = Order.builder().orderId("ORDER-003").user(user).build();
        em.persist(order);
        em.persist(Delivery.builder().order(order).deliveryStatus(DeliveryStatus.DELIVERED).build());
        OrderItem orderItem = OrderItem.builder().product(product).order(order).price(10000).quantity(1).color("BLACK").build();
        em.persist(orderItem);
        em.persist(Review.builder().orderItem(orderItem).user(user).content("리뷰내용").build());
        em.flush();
        em.clear();

        Page<GetWritableUserOrderItemResponseVO> result =
                reviewRepository.queryWritableOrderItemsByUser(user, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void 다른_사용자_주문상품은_조회안된다() {
        User other = User.builder().build();
        em.persist(other);
        Order order = Order.builder().orderId("ORDER-004").user(other).build();
        em.persist(order);
        em.persist(Delivery.builder().order(order).deliveryStatus(DeliveryStatus.DELIVERED).build());
        em.persist(OrderItem.builder().product(product).order(order).price(10000).quantity(1).color("BLACK").build());
        em.flush();
        em.clear();

        Page<GetWritableUserOrderItemResponseVO> result =
                reviewRepository.queryWritableOrderItemsByUser(user, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void 소프트삭제된_주문상품은_조회안된다() {
        Order order = Order.builder().orderId("ORDER-005").user(user).build();
        em.persist(order);
        em.persist(Delivery.builder().order(order).deliveryStatus(DeliveryStatus.DELIVERED).build());
        em.persist(OrderItem.builder().product(product).order(order).price(10000).quantity(1).color("BLACK").isDeleted(true).build());
        em.flush();
        em.clear();

        Page<GetWritableUserOrderItemResponseVO> result =
                reviewRepository.queryWritableOrderItemsByUser(user, PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isZero();
    }
}
