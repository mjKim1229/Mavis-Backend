package com.mavis.api.config;

import com.mavis.domain.domains.delivery.domain.QDelivery;
import com.mavis.domain.domains.order.domain.QOrderItem;
import com.mavis.domain.domains.product.domain.QProduct;
import com.mavis.domain.domains.product.domain.QProductImage;
import com.mavis.domain.domains.review.domain.QReview;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class QueryDSLConfig {

    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
//        QReview qReview = QReview.review;
//        QOrderItem qOrderItem = QOrderItem.orderItem;
//        QProduct qProduct = QProduct.product;
//        QProductImage qProductImage = QProductImage.productImage;
//        QDelivery qDelivery = QDelivery.delivery;
//        log.debug("QueryDSLConfig initialized");
        return new JPAQueryFactory(entityManager);
    }
}
