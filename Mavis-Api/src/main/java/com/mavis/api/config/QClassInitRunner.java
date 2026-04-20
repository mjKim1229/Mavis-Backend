package com.mavis.api.config;

import com.mavis.domain.domains.order.domain.QOrderItem;
import com.mavis.domain.domains.review.domain.QReview;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class QClassInitRunner implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) {
        log.info(">>>> [Reference Fix] Pre-loading QClasses to prevent deadlock...");
        QReview q1 = QReview.review;
        QOrderItem q2 = QOrderItem.orderItem;
        log.info(">>>> [Reference Fix] QClasses loaded successfully.");
    }
}
